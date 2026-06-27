package com.davdavtyan.notification;

import com.kafka.avro.AssignmentNotification;
import com.kafka.avro.Student;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssignmentConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "${kafka.topic.assignment}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(AssignmentNotification notification) {
        String mentorName = notification.getMentor().getMentorName();
        String mentorEmail = notification.getMentor().getMentorEmail();

        List<Student> students = notification.getStudents();

        for (Student student : students) {
            String studentMessage = String.format(
                "Hello, %s! You have been assigned to mentor %s.",
                student.getStudentName(),
                mentorName
            );
            emailService.sendEmail(student.getStudentEmail(), "New Mentor Assignment", studentMessage);
        }

        String studentNames = students.stream()
            .map(Student::getStudentName)
            .collect(Collectors.joining(", "));

        String mentorMessage = String.format(
            "Hello, %s! You have been assigned the following student(s): %s.",
            mentorName,
            studentNames
        );

        emailService.sendEmail(mentorEmail, "New Students Assignment", mentorMessage);
    }

}
