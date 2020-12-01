package com.atguigu.gmall.oms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallOmsApplicationTests {

    @Test
    void contextLoads() {
    }
    @Test
    void test(){
        int[] a = new int[]{2,3,9,5,36,5,3,454,5};
        for (int i = 0; i <a.length ; i++) {
            if(a[i]<a[i+1]){
                int b =a[i];
                a[i]=a[i+1];
                a[i+1]=b;
            }
        }
        System.out.println(a.toString());
    }

}
