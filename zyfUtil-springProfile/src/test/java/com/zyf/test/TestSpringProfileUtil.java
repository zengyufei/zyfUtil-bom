package com.zyf.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import com.zyf.utils.SpringProfileUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

@Slf4j
public class TestSpringProfileUtil {

    @Test
    public void testUtilLocal1() throws FileNotFoundException {
        // 默认 local 读取 mapping.ini 切文件正常存在
        SpringProfileUtil.tryCopyConfsFromMapping();
        Assert.assertTrue(FileUtil.exist(ClassUtil.getClassPath() + "test.properties"));
    }

    @Test
    public void testUtilLocal2() throws FileNotFoundException {
        // 默认 local 不读取 mapping.ini，文件 存在
        SpringProfileUtil.tryCopyConfsFromNotMapping();
    }

    @Test(expected = FileNotFoundException.class)
    public void testUtilDev1() throws FileNotFoundException {
        // dev 读取 mapping.ini，但实际 文件 不存在
        SpringProfileUtil.tryCopyConfsFromMapping("dev");
    }

    @Test(expected = FileNotFoundException.class)
    public void testUtilDev2() throws FileNotFoundException {
        // dev 不读取 mapping.ini，但实际 文件 不存在
        SpringProfileUtil.tryCopyConfsFromNotMapping("dev");
    }

    @Test(expected = FileNotFoundException.class)
    public void testUtilTest() throws FileNotFoundException {
        // test 读取 mapping.ini，但实际 文件夹 不存在
        SpringProfileUtil.tryCopyConfsFromMapping("test");
    }

}
