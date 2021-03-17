import com.chiaki.entity.Student;
import com.chiaki.mapper.StudentMapper;
import com.chiaki.mypagehelper.MyPageHelper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * MyBatis快速案例
 * @author chenliang258
 * @date 2021/3/3 13:59
 */
public class StudentTest {

    private InputStream in;
    private SqlSession sqlSession;

    @Before
    public void init() throws IOException {
        // 读取MyBatis的配置文件
        in = Resources.getResourceAsStream("mybatis-config.xml");
        // 创建SqlSessionFactory的构建者对象
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        // 使用builder创建SqlSessionFactory对象
        SqlSessionFactory factory = builder.build(in);
        // 使用factory创建sqlSession对象并设置自动提交事务
        sqlSession = factory.openSession(true);
    }

    @Test
    public void testSqlExecute() {
        // 使用sqlSession创建StudentMapper接口的代理对象
        StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
        // 使用代理对象执行相关方法
        Student student = studentMapper.getStudentById(2);
        System.out.println(student);
    }

    @Test
    public void getAllStudents() {
        // 定义分页相关参数
        int pageNum = 1, pageSize = 3;
        StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
        // 使用分页插件
        PageHelper.startPage(pageNum, pageSize);
        List<Student> students = studentMapper.findAll();
        System.out.println(students);
    }

    @Test
    public void testPage() {
        // 定义分页相关参数
        int pageNum = 2, pageSize = 3;
        // 准备List<Student>集合
        List<Student> students = new ArrayList<>();
        students.add(new Student(1, "张A","男"));
        students.add(new Student(2, "张B","男"));
        students.add(new Student(3, "张C","男"));
        students.add(new Student(4, "张D","男"));
        students.add(new Student(5, "张E","男"));
        // 分页
        List<Student> results = pageRequest(pageNum, pageSize, students);
        System.out.println(results);
    }

    @Test
    public void testMyPageHelper() {
        Integer pageNum = 2, pageSize = 3;
        StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
        MyPageHelper.startPage(pageNum, pageSize);
        List<Student> students = studentMapper.findAll();
        System.out.println(students);
    }

    @After
    public void close() throws IOException {
        // 关闭资源
        sqlSession.close();
        in.close();
    }

    /**
     * 分页请求
     * @param pageNum 指定页码
     * @param pageSize 指定每页的记录数
     * @param list 待分页的集合
     * @param <T> 集合类型
     * @return 分页后的集合
     */
    private <T> List<T> pageRequest(int pageNum, int pageSize, List<T> list){
        // 根据pageNum和pageSize构建Page类
        Page<T> page = new Page<T>(pageNum, pageSize);
        // 设置page对象的总记录数属性
        page.setTotal(list.size());
        // 计算分页的开始和结束索引
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());
        // 从待分页的集合获取需要展示的内容添加到page对象
        page.addAll(list.subList(startIndex, endIndex));
        // 返回分页后的集合
        return page.getResult();
    }
}
