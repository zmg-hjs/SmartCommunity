// miniprogram/pages/newsDetail/newsDetail.js
const app = getApp()
Page({

  /**
   * 页面的初始数据
   */
  data: {
    id: '',
    createDate: '',
    content: '',
    title: '',
    user: ''
    },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (e) {
    var that = this;
    that.setData({
      id: e.id
    })
    wx.request({
      url: app.globalData.domainName + '/sc/property/news/resident_news_one_data',
      method: 'POST',
      data: {
        id: e.id
      },
      success: function (res) {
        console.log(res.data)
        that.setData({
          createDate: res.data.data.createDateStr,
          title: res.data.data.title,
          user: res.data.data.staffUserActualName,
          content: res.data.data.content.replace(/\<img/gi, '<img style="max-width:100%;height:auto"')
        })
      }
    })
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {

  }
})