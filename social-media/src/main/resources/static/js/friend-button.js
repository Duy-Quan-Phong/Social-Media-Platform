document.addEventListener("DOMContentLoaded", () => {

    // Hàm gửi request chung
    const sendRequest = async (url, method, username, successMessage, errorMessage) => {
        try {
            const response = await fetch(url, {
                method,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({user_name: username})
            });

            if (response.ok) {
                location.reload();
            } else {
                throw new Error(errorMessage);
            }
        } catch (error) {
            console.error('Lỗi:', error);
            alert(errorMessage);
        }
    };

    // Cấu hình các endpoint và thông báo
    const buttonConfigs = {
        addFriend: {
            url: '/addFriend',
            method: 'POST',
            errorMessage: 'Có lỗi xảy ra khi gửi lời mời!'
        },
        acceptFriend: {
            url: '/acceptFriend',
            method: 'PUT',
            errorMessage: 'Có lỗi xảy ra khi chấp nhận lời mời!'
        },
        deleteActions: {
            url: '/deleteFriend',
            method: 'DELETE',
            errorMessage: 'Có lỗi xảy ra khi xóa bạn!'
        }
    };

    // Gắn sự kiện cho nút thêm bạn
    $(document).on('click', '.addFriendBtn', function () {
        const username = $(this).data('username');
        sendRequest(
            buttonConfigs.addFriend.url,
            buttonConfigs.addFriend.method,
            username,
            'Thêm bạn thành công',
            buttonConfigs.addFriend.errorMessage
        );
    });

    // Gắn sự kiện cho nút chấp nhận bạn
    $(document).on('click', '.acceptFriendBtn', function () {
        const username = $(this).data('username');
        sendRequest(
            buttonConfigs.acceptFriend.url,
            buttonConfigs.acceptFriend.method,
            username,
            'Xóa thành công',
            buttonConfigs.acceptFriend.errorMessage
        );
    });


    // Gắn sự kiện cho các nút hủy/từ chối/xóa bạn
    $(document).on('click', '.cancelFriendRequestBtn, .reflectFriendBtn, .deleteFriendBtn', function () {
        Swal.fire({
            title: "Xóa kết bạn?",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Xóa",
            cancelButtonText: "Hủy"
        }).then((result) => {
            if (result.isConfirmed) {
                const username = $(this).data('username');
                sendRequest(
                    buttonConfigs.deleteActions.url,
                    buttonConfigs.deleteActions.method,
                    username,
                    'Xóa thành công',
                    buttonConfigs.deleteActions.errorMessage
                );
            }
        });
    });


});