package sxx.xwl.community.community.dao;

import org.springframework.stereotype.Repository;

/**
 * @author sxx_27
 * @create 2022-04-18 14:25
 */
@Repository("hibernate")
public class AlphaDaoHibernateImpl implements AlphaDao{

    @Override
    public String selsect() {
        return "selsect";
    }
}
