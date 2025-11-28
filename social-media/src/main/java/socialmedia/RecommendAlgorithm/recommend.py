import mysql.connector
import networkx as nx
import sys

# --- CẤU HÌNH DATABASE (Sửa lại password nếu khác) ---
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '2352004q', # <-- MẬT KHẨU CỦA BẠN
    'database': 'social_media'
}

def get_db_connection():
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        return conn
    except mysql.connector.Error as err:
        print(f"Lỗi kết nối DB: {err}")
        sys.exit(1)

# --- 1. THUẬT TOÁN GỢI Ý KẾT BẠN (GRAPH-BASED) ---
def run_friend_recommendation(cursor):
    print("\n[1/3] Đang chạy Gợi ý Kết bạn (Graph Algorithm)...")

    # A. Xây dựng đồ thị từ dữ liệu bạn bè thật
    cursor.execute("SELECT requester_id, addressee_id FROM friendships WHERE status='ACCEPTED'")
    friendships = cursor.fetchall()

    G = nx.Graph()
    G.add_edges_from(friendships)

    # Lấy danh sách tất cả User
    cursor.execute("SELECT id FROM users")
    all_users = [row[0] for row in cursor.fetchall()]

    recommendations = []

    for user_id in all_users:
        # Nếu user chưa có trong đồ thị (chưa kết bạn với ai), bỏ qua bước Graph
        if user_id not in G: continue

        # Tìm "Bạn của bạn" (Friend of Friend)
        # Logic: Những người cách mình 2 bước nhảy mà chưa kết bạn trực tiếp
        candidates = set()
        for friend in G.neighbors(user_id):
            for friend_of_friend in G.neighbors(friend):
                if friend_of_friend != user_id and not G.has_edge(user_id, friend_of_friend):
                    candidates.add(friend_of_friend)

        # Chấm điểm ứng viên
        for candidate_id in candidates:
            # Feature: Số bạn chung
            common_friends = list(nx.common_neighbors(G, user_id, candidate_id))
            num_common = len(common_friends)

            # Công thức tính điểm đơn giản: 1 bạn chung = 0.5 điểm
            score = min(num_common * 0.5, 5.0) # Max 5 điểm

            if score > 0:
                reason = f"Có {num_common} bạn chung"
                recommendations.append((user_id, candidate_id, score, reason))

    # B. Lưu vào Database
    # Xóa gợi ý cũ
    cursor.execute("TRUNCATE TABLE friend_recommendations")

    if recommendations:
        sql = "INSERT INTO friend_recommendations (user_id, suggested_user_id, score, reason) VALUES (%s, %s, %s, %s)"
        cursor.executemany(sql, recommendations)
        print(f"   -> Đã lưu {len(recommendations)} gợi ý kết bạn.")
    else:
        print("   -> Không tìm thấy gợi ý kết bạn nào mới.")


# --- 2. THUẬT TOÁN GỢI Ý BÀI VIẾT (CONTENT-BASED) ---
def run_post_recommendation(cursor):
    print("\n[2/3] Đang chạy Gợi ý Bài viết (Interest Matching)...")

    # Lấy tất cả user
    cursor.execute("SELECT id FROM users")
    all_users = [row[0] for row in cursor.fetchall()]

    post_recs = []

    for user_id in all_users:
        # A. Lấy sở thích của User (Top 5 hashtag điểm cao nhất)
        cursor.execute(f"""
            SELECT hashtag_id, interest_score FROM user_hashtag_interests 
            WHERE user_id = {user_id} AND interest_score > 0
            ORDER BY interest_score DESC LIMIT 5
        """)
        interests = cursor.fetchall() # Dạng [(tag_id, score), ...]

        if not interests: continue # User này chưa like gì cả -> Bỏ qua

        favorite_tag_ids = [row[0] for row in interests]

        # B. Tìm bài viết chứa các hashtag đó (mà user chưa xem/tạo)
        # Giả sử ta gợi ý các bài viết PUBLIC
        format_strings = ','.join(['%s'] * len(favorite_tag_ids))
        query = f"""
            SELECT DISTINCT p.id, ph.hashtag_id, h.name
            FROM posts p
            JOIN post_hashtags ph ON p.id = ph.post_id
            JOIN hashtags h ON ph.hashtag_id = h.id
            WHERE ph.hashtag_id IN ({format_strings})
            AND p.user_id != {user_id} 
            AND p.privacy_level = 'PUBLIC'
            LIMIT 10
        """
        cursor.execute(query, tuple(favorite_tag_ids))
        potential_posts = cursor.fetchall()

        # C. Chấm điểm bài viết
        for post in potential_posts:
            post_id = post[0]
            tag_name = post[2]

            # Logic: Điểm gợi ý = 0.9 (Cố định cho demo) + 1 chút random để thay đổi
            score = 0.95
            reason = f"Vì bạn quan tâm #{tag_name}"

            post_recs.append((user_id, post_id, score, reason))

    # D. Lưu vào Database
    cursor.execute("TRUNCATE TABLE post_recommendations")

    if post_recs:
        # Dùng INSERT IGNORE để tránh lỗi trùng lặp nếu 1 bài có 2 hashtag user đều thích
        sql = "INSERT IGNORE INTO post_recommendations (user_id, post_id, score, reason) VALUES (%s, %s, %s, %s)"
        cursor.executemany(sql, post_recs)
        print(f"   -> Đã lưu {len(post_recs)} bài viết đề xuất.")
    else:
        print("   -> Không có bài viết nào phù hợp sở thích.")


# --- 3. LOGIC LÀM NGUỘI SỞ THÍCH (TIME DECAY) ---
def apply_time_decay(cursor, conn):
    print("\n[3/3] Đang làm nguội sở thích (Time Decay)...")
    # Giảm 10% điểm sở thích mỗi lần chạy
    cursor.execute("UPDATE user_hashtag_interests SET interest_score = interest_score * 0.9")
    # Xóa sở thích quá thấp
    cursor.execute("DELETE FROM user_hashtag_interests WHERE interest_score < 0.5")
    conn.commit()
    print("   -> Đã cập nhật xong.")


# --- CHẠY CHÍNH ---
if __name__ == "__main__":
    connection = get_db_connection()
    db_cursor = connection.cursor()

    try:
        # Chạy lần lượt các thuật toán
        apply_time_decay(db_cursor, connection)
        run_friend_recommendation(db_cursor)
        run_post_recommendation(db_cursor)

        connection.commit()
        print("\n=== HOÀN TẤT! AI ENGINE ĐÃ CẬP NHẬT DATABASE ===")

    except Exception as e:
        print(f"Có lỗi xảy ra: {e}")
    finally:
        db_cursor.close()
        connection.close()