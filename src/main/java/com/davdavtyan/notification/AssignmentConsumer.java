package com.davdavtyan.notification;

import com.kafka.avro.AssignmentNotification;
import com.kafka.avro.Student;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "${kafka.topic.assignment}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(AssignmentNotification notification) {

        log.warn("Send notification");

        String mentorName = notification.getMentor().getMentorName();
        String mentorEmail = notification.getMentor().getMentorEmail();

        List<Student> students = notification.getStudents();
        if (students == null || students.isEmpty()) {
            log.warn("Assignment notification for mentor {} has no students, skipping", mentorEmail);
            return;
        }

        for (Student student : students) {
            String studentMessage = String.format(
                "Hello, %s! You have been assigned to mentor %s.",
                student.getStudentName(),
                mentorName
            );
            sendSafely(student.getStudentEmail(), "New Mentor Assignment", studentMessage);
        }

        String studentNames = students.stream()
            .map(Student::getStudentName)
            .collect(Collectors.joining(", "));

        String mentorMessage = String.format(
            "Hello, %s! You have been assigned the following student(s): %s.",
            mentorName,
            studentNames
        );

        sendSafely(mentorEmail, "New Students Assignment", mentorMessage);
    }

    private void sendSafely(String to, String subject, String body) {
        try {
            emailService.sendEmail(to, subject, body);
        } catch (Exception e) {
            log.error("Failed to send notification email to {}", to, e);
        }
    }

}
