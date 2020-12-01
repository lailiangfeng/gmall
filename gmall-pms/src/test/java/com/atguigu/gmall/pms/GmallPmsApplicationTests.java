package com.atguigu.gmall.pms;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.dao.BrandDao;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@SpringBootTest
class GmallPmsApplicationTests {
    @Autowired
    private BrandService brandService;
    private QueryCondition queryCondition;
    DataSource dataSource;
    @Resource
    private BrandDao brandDao;
    @Test
    void contextLoads() {
        System.out.println(dataSource.getClass());
//        Integer spuId =1;
//        System.out.println(spuId);

    }
    @Test
    public void getBrandById(){
        Integer brandId =4;
        //BrandEntity brand = brandService.getById(brandId);
        System.out.println(brandId);
//        PageVo page = brandService.queryPage(queryCondition);
//        System.out.println(page);
    }
    @Test
    public void selectBrand(){
       // System.out.println(brandDao.selectBrand(4));
    }
    @Test
    public void testChange(){
      //list--->array
//        ArrayList<String> list = new ArrayList<>();
//        list.add("张三");
//        list.add("李四");
//        list.add("王武");
//        int size = list.size();
//        String[] strings = list.toArray(new String[size]);
//        System.out.println(JSON.toJSONString(strings));//["张三","李四","王武"]


        //array--->list
//        String[] s = {"tom","jeery"};
//        List<String> strings1 = Arrays.asList(s);
//        List<String> strings2 = Arrays.asList("aaa", "bbb");
//        System.out.println(JSON.toJSONString(strings1));//["tom","jeery"]

        //list---->set
        ArrayList<String> strings = new ArrayList<>(new HashSet<String>());

        ClassLoader classLoader = this.getClass().getClassLoader();


    }
    @Test
    public void testStream(){
//        ArrayList<String> strings = new ArrayList<>();
//            strings.add("zhangshan0");
//            strings.add("zhangshan1");
//            strings.add("zhangshan2");
//            strings.add("zhangshan3");
//            strings.stream().forEach(n->System.out.println(n));
        Stream<String> str = Stream.of("1", "2", "3");
        Stream<Integer> integerStream = str.map((String s) -> {
            return Integer.parseInt(s);
        });
        System.out.println(integerStream.toString());

    }
    @Test
    public void testStream1(){
        ArrayList<String> team1 = new ArrayList<String>();
        ArrayList<String> team2 = new ArrayList<String>();
        team1.add("张山风");
        team1.add("张无极43");
        team1.add("刘语熙32");
        team1.add("张二狗");
        team1.add("李老汉3");
        team1.add("孙机械");
        team1.add("孙机s");

        team2.add("张山风");
        team2.add("张无极43");
        team2.add("刘语熙32");
        team2.add("张二狗");
        team2.add("李老汉3");
        team2.add("孙机械");
        team2.add("孙机s");
         ArrayList<String> teamn1 = new ArrayList<>();
         ArrayList<String> teamn2 = new ArrayList<>();
         ArrayList<String> teamn3 = new ArrayList<>();


        for (int i = 0; i < team1.size(); i++) {
            if(team1.get(i).length()<=3){
                teamn1.add(team1.get(i));
            }
        }

        for (int i = 0; i < 3; i++) {
            teamn2.add(teamn1.get(i));
        }


    }

    @Test
    public void test(){
        int[] numbers = new int[]{6,2,3,9,5,36,5,3,454,5};
        for (int j = 0; j <numbers.length-1 ; j++) {
            for (int i = 0; i < numbers.length-1-j; i++) {
                if (numbers[i] > numbers[i+1]) {
                    int b = numbers[i];
                    numbers[i] = numbers[i+1];
                    numbers[i+1] = b;
                }
            }
        }
        for (int name:numbers) {
            System.out.println(name);
        }
    }
    @Test
    public void test1(){
        //冒泡排序算法
        int[] numbers=new int[]{6,2,3,9,5,36,5,3,454,5};
        //需进行length-1次冒泡
        for(int i=0;i<numbers.length-1;i++)
        {
            for(int j=0;j<numbers.length-1-i;j++)
            {
                if(numbers[j]>numbers[j+1])
                {
                    int temp=numbers[j];
                    numbers[j]=numbers[j+1];
                    numbers[j+1]=temp;
                }
            }
        }
        System.out.println("从小到大排序后的结果是:");
        for (int aa:numbers) {
            System.out.println(aa);
        }
//        for(int i=0;i<numbers.length;i++){
//            System.out.print(numbers[i]+" ");
//        }

    }
    @Test
    public void testStrChange() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("nide ");
        System.out.println(scanner.next());


    }
    @Test
    public void copyFile() throws IOException {
        char a='5';
        String s = String.valueOf(a);
        int i = Integer.parseInt(s);

        FileInputStream fileInputStream = new FileInputStream("/Users/liwanqing/Desktop/java学习笔记/rabbitmq.docx");
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/liwanqing/Desktop/java学习笔记/rabbitmqcopy.docx");
        byte[] bytes = new byte[1024];
        int len=0;
        while ((len = fileInputStream.read(bytes))!=-1){

            fileOutputStream.write(bytes,0,len);
        }
        fileOutputStream.close();
        fileInputStream.close();

    }
    @Test
    public void testSdk9() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("/Users/liwanqing/Desktop/java学习笔记/rabbitmq.docx");
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/liwanqing/Desktop/java学习笔记/rabbitmqcopy.docx");
        try (fileInputStream;fileOutputStream){
            System.out.println(1);
        }catch (Exception e){
            e.getMessage();
        }


    }

    //@Test
    public static void main2(String[] args) {
        int[] a = {10,2,343,545,56,67};
        int[] b =new int[4];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length-1; j++) {
                int c;
                if (a[j]>a[j+1]){
                    c = a[j];
                    a[j]=a[j+1];
                    a[j+1]=c;
                }
            }
        }

        for (int aaa:
                a) {
            System.out.println(aaa);
        }
    }
    public static void main(String[] args) {
       // new ConcurrentHashMap()
//        JdbcTemplate jdbcTemplate = new JdbcTemplate();
//        jdbcTemplate.update("delete from account where name=?","tom");
    }




}
