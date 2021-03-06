//index.js
//获取应用实例
const app = getApp()

Page({
  data: {
    userInfo:'',
  },
  //事件处理函数
  service(e){
    let type = e.currentTarget.dataset.type
    var _url
    if (type == 'chat') {
      _url = "/pages/chat/im/chat"
    } else if (type == 'emal') {
      _url = "/pages/emal/emal"
    } else if (type == 'car') {
      _url = "/pages/car/car"
    }else if(type == 'vote'){
      _url="/pages/vote/vote"
    }else if(type == 'repair'){
      _url="/pages/repair/repair"
    }else if(type == 'shop'){
      _url="/pages/shop/index"
    }else if(type == 'fbk'){
      _url="/pages/feedback/index"
    }else if(type == 'charges'){
      _url="/pages/charges/index"
    }
    wx.navigateTo({
      url: _url,
    })

  },
  onLoad: function () {
    this.setData({
      userInfo:wx.getStorageSync('userInfo')
    })
    },
  onShow: function () {
    this.setData({
      userInfo:wx.getStorageSync('userInfo')
    })
    }
  })
 
