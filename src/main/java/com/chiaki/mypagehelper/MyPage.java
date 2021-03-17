package com.chiaki.mypagehelper;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页返回对象类
 * @author chenliang258
 * @date 2021/3/17 17:11
 */
@Getter
public class MyPage<E> extends ArrayList<E> {

  private static final long serialVersionUID = 2630741492557235098L;

  /**  指定页码，从1开始  **/
  @Setter
  private Integer pageNum;

  /**  指定每页记录数  **/
  @Setter
  private Integer pageSize;

  /**  起始行  **/
  @Setter
  private Integer startIndex;

  /**  末行  **/
  @Setter
  private Integer endIndex;

  /**  总记录数  **/
  private Long total;

  /**  总页数  **/
  @Setter
  private Integer pages;

  public void setTotal(Long total) {
    this.total = total;
    this.pages = (int)(total / pageSize + (total % pageSize == 0 ? 0 : 1));
    if (pageNum > pages) {
      pageNum = pages;
    }
    this.startIndex = this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0;
    this.endIndex = this.startIndex + this.pageSize * (this.pageNum > 0 ? 1 : 0);

  }

  public List<E> getResults() {
    return this;
  }
}
