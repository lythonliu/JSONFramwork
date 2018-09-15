//package com.example.administrator.jsonframwork;
//
//import java.util.List;
//
///**
// * Created by Administrator on 2017/2/22 0022.
// */
//
//public class Test {
//    public void test()
//    {
//        Box<? super Number> box=new Box<>();
//        box.setData(new Object());
//        /**
//         *  object
//         */
////        Number box1=box.getData();  extends  get()  用于限制返回值的
////        box.setData(new Integer(1));super    set()  用于参数类型的限定
////        box.setData(new Float(2f));
//        //Integer是Number子类
//        /**
//         * Box<Number>不是 Box<Intger><的父类
//         * 苹果 也是水果
//         * 装满苹果的盘子不是装满水果的盘子
//         *
//         * 实际生活体验  ：装满水果的盘子也能装苹果
//         * 1 会
//         * 2 不会
//         */
////         box=new Box<Integer>();
////         box=new Box<Object>();
////        box=new Box<Float>();
////        box=new Box<Double>();
////        box=new Box<Object>();
////        box.setData(new Number() {
//         List<? extends Fruit> list=new ArrayList<? extends Fruit>();
////        });
//        /*
//        1
//        2
//         */
//
//    }
//
//
//}
