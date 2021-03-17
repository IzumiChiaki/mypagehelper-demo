package com.chiaki.mypagehelper;

/**
 * 分页帮助类
 * @author chenliang258
 * @date 2021/3/17 17:23
 */
@SuppressWarnings("rawtypes")
public class MyPageHelper {

    private static final ThreadLocal<MyPage> MY_PAGE_THREAD_LOCAL = new ThreadLocal<>();

    public static void setMyPageThreadLocal(MyPage myPage) {
        MY_PAGE_THREAD_LOCAL.set(myPage);
    }

    public static MyPage geyMyPageThreadLocal() {
        return MY_PAGE_THREAD_LOCAL.get();
    }

    public static void clearMyPageThreadLocal() {
        MY_PAGE_THREAD_LOCAL.remove();
    }

    public static void startPage(Integer pageNum, Integer pageSize) {
        MyPage myPage = new MyPage();
        myPage.setPageNum(pageNum);
        myPage.setPageSize(pageSize);
        setMyPageThreadLocal(myPage);
    }
}
