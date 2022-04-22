package sxx.xwl.community.community.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sxx.xwl.community.community.dao.AlphaDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
/**
 * @author sxx_27
 * @create 2022-04-18 14:37
 */
@Service
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public String find(){
        return alphaDao.selsect();
    }

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("销毁AlphaService");
    }

}
