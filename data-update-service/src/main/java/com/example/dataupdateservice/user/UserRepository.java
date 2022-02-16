package com.example.dataupdateservice.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("Select user from User user where user.email =:email and user.password =:password")
    User getUserByCredentials(@Param("email") String email, @Param("password") String password);

}
