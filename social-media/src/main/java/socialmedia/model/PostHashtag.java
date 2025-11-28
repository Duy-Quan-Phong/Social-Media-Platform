

package socialmedia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import socialmedia.model.social_action.Post;

@Entity
@Table(name = "post_hashtags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostHashtag {

    @EmbeddedId
    private PostHashtagId id;

    @ManyToOne
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @MapsId("hashtagId")
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;

    // Constructor quan trọng để dùng trong Service
    public PostHashtag(Post post, Hashtag hashtag) {
        this.id = new PostHashtagId(post.getId(), hashtag.getId());
        this.post = post;
        this.hashtag = hashtag;
    }
}