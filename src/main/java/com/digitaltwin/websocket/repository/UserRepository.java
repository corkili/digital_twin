package com.digitaltwin.websocket.repository;

import com.digitaltwin.websocket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户数据访问层接口
 * 提供用户实体的CRUD操作
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    User findByUsername(String username);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 如果用户名已存在则返回true，否则返回false
     */
    boolean existsByUsername(String username);
}