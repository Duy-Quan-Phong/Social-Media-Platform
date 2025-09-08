package com.codegym.socialmedia.component;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    public String upload(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            // Lấy đường dẫn an toàn (https)
            return (String) uploadResult.get("secure_url");
        } catch (IOException ex) {
            System.out.println("Upload lỗi: " + ex.getMessage());
            return null; // hoặc throw exception tùy cách xử lý của bạn
        }
    }

}