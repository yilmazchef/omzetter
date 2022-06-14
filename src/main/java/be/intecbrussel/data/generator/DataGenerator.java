package be.intecbrussel.data.generator;

import be.intecbrussel.data.Role;
import be.intecbrussel.data.entity.Download;
import be.intecbrussel.data.entity.Upload;
import be.intecbrussel.data.entity.User;
import be.intecbrussel.data.service.DownloadRepository;
import be.intecbrussel.data.service.UploadRepository;
import be.intecbrussel.data.service.UserRepository;
import com.vaadin.exampledata.DataType;
import com.vaadin.exampledata.ExampleDataGenerator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringComponent
public class DataGenerator {

        @Bean
        public CommandLineRunner loadData(PasswordEncoder passwordEncoder, UserRepository userRepository,
                        UploadRepository uploadRepository, DownloadRepository downloadRepository) {
                return args -> {
                        Logger logger = LoggerFactory.getLogger(getClass());
                        if (userRepository.count() != 0L) {
                                logger.info("Using existing database");
                                return;
                        }
                        int seed = 123;

                        logger.info("Generating demo data");

                        logger.info("... generating 2 User entities...");
                        User user = new User();
                        user.setName("John Normal");
                        user.setUsername("user");
                        user.setHashedPassword(passwordEncoder.encode("user"));
                        user.setProfilePictureUrl(
                                        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
                        user.setRoles(Collections.singleton(Role.USER));
                        userRepository.save(user);
                        User admin = new User();
                        admin.setName("Emma Powerful");
                        admin.setUsername("admin");
                        admin.setHashedPassword(passwordEncoder.encode("admin"));
                        admin.setProfilePictureUrl(
                                        "https://images.unsplash.com/photo-1607746882042-944635dfe10e?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
                        admin.setRoles(Set.of(Role.USER, Role.ADMIN));
                        userRepository.save(admin);
                        logger.info("... generating 100 Upload entities...");
                        ExampleDataGenerator<Upload> uploadRepositoryGenerator = new ExampleDataGenerator<>(
                                        Upload.class,
                                        LocalDateTime.of(2022, 6, 14, 0, 0, 0));
                        uploadRepositoryGenerator.setData(Upload::setFilename, DataType.BOOK_TITLE);
                        uploadRepositoryGenerator.setData(Upload::setExtension, DataType.WORD);
                        uploadRepositoryGenerator.setData(Upload::setCreatedBy, DataType.EMAIL);
                        uploadRepositoryGenerator.setData(Upload::setUpdatedBy, DataType.EMAIL);
                        uploadRepositoryGenerator.setData(Upload::setCreatedAt, DataType.DATETIME_LAST_30_DAYS);
                        uploadRepositoryGenerator.setData(Upload::setUpdatedAt, DataType.DATETIME_LAST_7_DAYS);
                        uploadRepositoryGenerator.setData(Upload::setExpiresAt, DataType.DATETIME_LAST_30_DAYS);
                        uploadRepositoryGenerator.setData(Upload::setAccessCode, DataType.EAN13);
                        uploadRepositoryGenerator.setData(Upload::setIsActive, DataType.BOOLEAN_50_50);
                        uploadRepositoryGenerator.setData(Upload::setDownloadCount, DataType.NUMBER_UP_TO_1000);
                        uploadRepository.saveAll(uploadRepositoryGenerator.create(100, seed));

                        logger.info("... generating 100 Download entities...");
                        ExampleDataGenerator<Download> downloadRepositoryGenerator = new ExampleDataGenerator<>(
                                        Download.class,
                                        LocalDateTime.of(2022, 6, 14, 0, 0, 0));
                        downloadRepositoryGenerator.setData(Download::setUpload, DataType.UUID);
                        downloadRepositoryGenerator.setData(Download::setRequestedBy, DataType.EMAIL);
                        downloadRepositoryGenerator.setData(Download::setRequestedAt, DataType.DATETIME_LAST_30_DAYS);
                        downloadRepositoryGenerator.setData(Download::setReceivedAt, DataType.DATETIME_LAST_10_YEARS);
                        downloadRepositoryGenerator.setData(Download::setConvertedTo, DataType.WORD);
                        downloadRepositoryGenerator.setData(Download::setScore, DataType.NUMBER_UP_TO_100);
                        downloadRepositoryGenerator.setData(Download::setIsActive, DataType.BOOLEAN_50_50);
                        downloadRepository.saveAll(downloadRepositoryGenerator.create(100, seed));

                        logger.info("Generated demo data");
                };
        }

}