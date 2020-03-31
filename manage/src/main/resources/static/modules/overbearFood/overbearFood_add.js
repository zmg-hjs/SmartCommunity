layui.config({
    base: '../../../static/' //静态资源所在路径
}).extend({
    index: 'lib/index' //主入口模块
    , formSelects: '../lib/formSelects/formSelects-v4'
}).use(['index', 'table', 'form', 'laydate', 'formSelects', 'upload'], function () {
    layui.form.config.verify.required[1] = requiredNotNull;
    var $ = layui.$,
        form = layui.form,
        laydate = layui.laydate,
        table = layui.table,
        upload = layui.upload,
        formSelects = layui.formSelects;


    laydate.render({
        elem: '#releaseDateStr'
        , type: 'datetime'
        , format: 'yyyy-MM-dd HH:mm:ss'
    });
    var nowDate = new Date();
    nowDate.setDate(nowDate.getDate() + 1)
    nowDate.setHours(23);
    nowDate.setMinutes(59);
    nowDate.setSeconds(59);
    laydate.render({
        elem: '#lastEndDateStr'
        , type: 'datetime'
        , format: 'yyyy-MM-dd'
        ,value:nowDate
        , ready: function (value) {

        }
        , done: function(value, date, endDate) {

        }
    });

    $(document).on('click', '.layui-btn.event-btn', function () {
        var type = $(this).data('type');
        active[type] ? active[type].call($(this)) : '';
    }).on('click', '.upload-img-item .layui-icon', function () {
        var url = $(this).parent().find('img').prop('src');
        console.log(url);
        var type = $(this).parent().remove()
    });


    /**
     * 获取所有图片路经
     */
    function getImgSrc() {

        var urlArr = []
        var arrObj = {}
        //根据图片的总类型生成数组，拼接属性名
        for (let i = 0; i < combinationNumber; i++) {
            arrObj['urlArr' + i] = [];
            $("img[data-key^='filePreview_']").each(function (index, item) {
                var picAddr = $(item).attr("pic_url_" + i)
                if (picAddr){
                    arrObj['urlArr' + i].push(picAddr)
                }
                if ($(item).attr("pic_url_f0")) {
                    urlArr.push($(item).attr("pic_url_f0"));
                }
            });
            $("input[name='combinationPicAddress_" + i + "']").val(arrObj['urlArr' + i].join(","));
        }
        //添加到对应的input
        $("input[name='foodAddressListStr']").val(urlArr.join(","));
    }

    layui.use('form', function () {

        //监听提交
        form.on('submit(component-form)', function (data) {

            getImgSrc();
            var searchObj = $("#searchFormId").serializeObject();
            //循环取出，循环次数为添加的标签个数 -1,通过添加后缀匹配
            var searchObjStr = JSON.stringify(searchObj);
            var combinationList = [];
            for (var i = 0; i < combinationNumber; i++) {
                var combinationName = "combinationName_" + i;
                var combinationPicAddress = "combinationPicAddress_" + i;
                var combinationType = "combinationType_" + i;
                if (searchObjStr.indexOf(combinationName)) {
                    var combination = {combinationName:"",combinationPicAddress:"",combinationType:""}
                    combination.combinationName = searchObj[combinationName]
                    combination.combinationPicAddress = searchObj[combinationPicAddress]
                    combination.combinationType = searchObj[combinationType]
                    combinationList.push(combination)
                }
            }
            var combinationListStr = JSON.stringify(combinationList);
            searchObj.combinationListStr = combinationListStr;
            //将对象添加到数组



            layer.open({
                title: '提示'
                ,content: '是否确认发布'
                ,btn:['确定','取消']
                ,yes:function(index, layero){
                    console.info("yes");
                    searchObj.serviceNoStr="view_overbearfood_add_submission";
                    $.simpleAjax('/view/v2/overbearfood/view_overbearfood_add_submission', 'POST', JSON.stringify(searchObj), "application/json;charset-UTF-8", returnFunction);
                    layer.close(index);
                }
                ,btn2:function (index) {
                    console.info("btn2");
                    layer.close(index);
                }
                ,cancel: function(index, layero){
                    console.info("cancel");
                }
            });





            return false;//这一行代码必须加，不然会自动刷新页面，这个和layui的封装有关，且returnFunction 也不会调用
        });
        form.render(); // 更新全部
    });

    function returnFunction(data) {
        if (data.code == '1') {
            layer.open({
                icon:1,
                title: advice
                ,content: data.msg
                ,yes: function(index, layero){
                    window.location.reload();
                }
            });
            return;

        } else {
            layer.msg(data.msg, {
                icon: 5,
                time: 2000 //2秒关闭（如果不配置，默认是3秒）
            });
        }
    }
    var combinationNumber = 1
    function combinationAdd(){
        var setMealHtml='<div id="combination_' + combinationNumber + '" >\n' +
            '                <div class="layui-row">\n' +
            '                    <div class="layui-col-xs6">\n' +
            '                        <div class="layui-form-item">\n' +
            '                            <label class="layui-form-label required-tag">餐品分类</label>\n' +
            '                            <div class="layui-input-block">\n' +
            '                                <input type="text" id="combinationType_' + combinationNumber + '" name="combinationType_' + combinationNumber + '" th:placeholder="请输入组合类型名"\n' +
            '                                       autocomplete="off"\n' +
            '                                       class="layui-input" lay-verify="required"/>\n' +
            '                            </div>\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '\n' +
            '                    <div class="layui-col-xs6">\n' +
            '                        <div class="layui-form-item">\n' +
            '                            <label  class="layui-form-label required-tag">餐品名</label>\n' +
            '                            <div class="layui-input-block">\n' +
            '                                <input type="text" id="combinationName_' + combinationNumber + '" name="combinationName_' + combinationNumber + '" th:placeholder="请输入组合名"\n' +
            '                                       autocomplete="off"\n' +
            '                                       class="layui-input" lay-verify="required"/>\n' +
            '                            </div>\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '                </div>\n' +
            '\n' +
            '                <div  class="edit-module-title">上传一张餐品图片</div>\n' +
            '                <div class="layui-upload">\n' +
            '                    <div id="combinationPicAddress_' + combinationNumber + '" class="layui-upload-list" >\n' +
            '                        <input name="combinationPicAddress_' + combinationNumber + '" type="hidden" value="" lay-verify="pic"/>\n' +
            '                        <div class="upload-img-item" >\n' +
            '\n' +
            '                        </div>\n' +
            '                        <div class="upload-item-add" id="upload_combinationPic_add_' + combinationNumber + '"><i class="layui-icon layui-icon-add-1"></i>\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '                </div>\n' +
            '                <button  id="combinationDelete_0" name="combinationDelete_0" type="button" class="layui-btn">移除组合</button>\n' +
            '            </div>'

        $('#combinationAdd').before(setMealHtml);
        var id = '#upload_combinationPic_add_' + combinationNumber
        uploadPic(id,combinationNumber);
        combinationNumber++;
    }
    function uploadPic(id,number){
        upload.render({
            elem: id
            , url: '/pic/ossFileUpload'
            , multiple: true
            , field: 'file'
            , before: function (obj) {
                //预读本地文件示例，不支持ie8
                obj.preview(function (index, file, result) {
                    var imgHtml = '<div style="display: none" class="upload-img-item" >\n' +
                        '                <i class="layui-icon layui-icon-close-fill"></i>\n' +
                        '                <img data-key="filePreview_' + fileIndex + '" src="' + result + '" style="border: 1px solid #c0c0c0;" />\n' +
                        '          </div>';
                    $(id).before(imgHtml);
                    fileIndex++;
                });
            }, done: function (data) {
                if (data.code == '1') {
                    var url = data.data;
                    $("img[data-key='filePreview_" + (fileIndex - 1) + "']").attr("pic_url_" + number , url);
                    $("img[data-key='filePreview_" + (fileIndex - 1) + "']").parent().css("display", 'inline-block');
                } else {
                    layer.msg(data.msg, {
                        icon: 5,
                        time: 2000 //2秒关闭（如果不配置，默认是3秒）
                    });
                }
            }
        });
    }
    //获取点击事件，（新增，删除标签栏）（图片上传）
    $(document).click(function (e) {
        var id = e.target.id
        var last = id.charAt(id.length - 1)
        if (id == 'combinationDelete_' + last) {
            var parent=document.getElementById("searchFormId");
            var son=document.getElementById(e.target.parentElement.id);
            parent.removeChild(son);
            combinationNumber--;
        }
        if (id == 'combinationAdd') {
            combinationAdd();
        }
    })
    var fileIndex = 0;
    layui.use('upload', function(){
        //执行实例
        var foodId = '#upload_foodPic_add'
        var foodNumber = 'f0'
        uploadPic(foodId,foodNumber)
        var combinationId = '#upload_combinationPic_add_0'
        var combinationNum = '0'
        uploadPic(combinationId,combinationNum)
    });

    //自定义验证规则
    form.verify({
        specifiedInstruction: function(value){
            if(value.length > 20){
                return '不能超过20字符';
            }
        }

    });

    function initData() {
        loadOwner($("select[name='owner']"));
    }

    // 初始化控件数据
    $(document).ready(function () {
        initData();
    });

})
;