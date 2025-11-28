package socialmedia.component;

import socialmedia.dto.NotificationDTO;
import socialmedia.model.social_action.Notification;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class NotificationMapper {
    public NotificationDTO toDto(Notification n) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = n.getCreatedAt().format(formatter);
        var s = n.getSender();
        return new NotificationDTO(
                n.getId(),
                n.getNotificationType().name(),
                formattedDate,
                n.getReferenceId(),
                n.getReferenceType().name(),
                new NotificationDTO.SenderDTO(s.getId(), s.getUsername(), s.getProfilePicture())
        );
    }
}