package pwd.allen.elastic.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 *
 * 如果有公共的方法，可以单独声明一个接口来给其他类实现
 *
 * @author 门那粒沙
 * @create 2022-05-04 11:39
 **/
@NoRepositoryBean
public interface MyBaseRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {

    @Override
    Optional<T> findById(ID id);
}
