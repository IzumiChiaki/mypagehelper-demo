package com.chiaki.mapper;

import com.chiaki.entity.Student;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author chenliang258
 * @date 2021/3/3 11:47
 */
public interface StudentMapper {

    /**
     * 查询表中所有记录
     * @return 所有记录集合
     */
    List<Student> findAll();

    /**
     * 根据学生ID查询学生信息
     * @param id 学生ID
     * @return 学生信息
     */

    Student getStudentById(int id);

    /**
     * 插入学生信息
     * @param student 学生信息封装
     * @return 插入成功条数
     */
    int insertStudent(Student student);

    /**
     * 根据学生ID更新姓名信息
     * @param name 学生姓名
     * @param id 学生ID
     * @return 更新成功条数
     */
    int updateStudentName(@Param("name") String name, @Param("id") int id);
}
