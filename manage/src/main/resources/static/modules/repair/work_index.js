layui.config({
    base: '../../../static/' //静态资源所在路径
}).extend({
    index: 'lib/index' //主入口模块
    , formSelects: '../lib/formSelects/formSelects-v4'
    , common: '../modules/common'
}).use(['index', 'table', 'form', 'laydate', 'formSelects', 'common'], function () {
    var $ = layui.$,
        form = layui.form,
        laydate = layui.laydate,
        table = layui.table,
        formSelects = layui.formSelects;
    element = layui.element;

    var url = ""
    //日期插件渲染
    laydate.render({
        elem: '#time'
        , type: 'date'
        , range: '~'
        , format: 'yyyy-MM-dd'
    });

    //表格渲染
    table.render({
        elem: '#order-table-toolbar'
        , url: '/sc/manage/repair/manage_work_index_data'
        , toolbar: '#order-table-toolbar-toolbarDemo'
        , title: ''
        , cols: [[
            {type: 'checkbox', field: 'id', fixed: 'left'}
            , {field: 'staffUserActualName', title: '维修人员姓名',  width: 200, align: 'center'}
            , {field: 'staffUserPositionStr', title: '职位',  width: 200, align: 'center'}
            , {field: 'workStatusStr', title: '工作状态',  width: 200, align: 'center'}
            , {field: 'repairNumber', title: '维修次数',  width: 200, align: 'center'}
            , {field: 'weight', title: '优先级',  width: 200, align: 'center'}
            , {field: 'staffUserId', title: '维修人员id',  width: 200, align: 'center',hide:true}
            , {field : 'tool',fixed: 'right',title : '操作',minWidth : 260,align : 'center',toolbar : '#barDemo'}
        ]]
        , page: true
        //回调函数查询不同状态数据总数
    });

    //监听搜索
    form.on('submit(LAY-user-front-search)', function (data) {
        var field = data.field;
        field['checkBoxIdList'] = null;
        //执行重载
        table.reload('order-table-toolbar', {
            where: field,
            page: {
                curr: 1
            }
        });
        return false;
    });

    //监听行工具事件
    table.on('tool(order-table-toolbar)', function (obj) {
        var data = obj.data;
        console.log(data.id)
        if (obj.event === 'find'){
            console.log(data.id)
            var width = document.documentElement.scrollWidth * 0.9 + "px";
            var height = document.documentElement.scrollHeight * 0.9 + "px";
            layer.open({
                type: 2,
                skin: 'open-class',
                area: [width, height],
                title: '详情',
                content: "/sc/manage/repair/manage_work_find_page?id="+data.id+"&&staffUserId="+data.staffUserId
                ,maxmin: true
                ,zIndex: layer.zIndex //重点1
            });
        }
    });

    //头工具栏事件
    table.on('toolbar(order-table-toolbar)', function(obj){
        var d = {};
        var t = $('#searchFormId').serializeArray();
        $.each(t, function() {
            d[this.name] = this.value;
        });
        switch(obj.event){
            //自定义头工具栏右侧图标 - 提示
            case 'all':
                //获取查询表单数据
                d.workStatus='';
                table.reload('order-table-toolbar', {
                    where: d,
                    page: {
                        curr: 1
                    }
                });
                $("#djA").css("background-color", "#b1b0b0");
                $("#djB").css("background-color", "#ffffff");
                $("#djC").css("background-color", "#ffffff");
                $("#djD").css("background-color", "#ffffff");
                break;
            case 'on_duty_status':
                //获取查询表单数据
                d.workStatus='on_duty_status';
                table.reload('order-table-toolbar', {
                    where: d,
                    page: {
                        curr: 1
                    }
                });
                $("#djB").css("background-color", "#b1b0b0");
                $("#djA").css("background-color", "#ffffff");
                $("#djC").css("background-color", "#ffffff");
                $("#djD").css("background-color", "#ffffff");
                break;
            case 'be_busy':
                //获取查询表单数据
                d.workStatus='be_busy';
                table.reload('order-table-toolbar', {
                    where: d,
                    page: {
                        curr: 1
                    }
                });
                $("#djC").css("background-color", "#b1b0b0");
                $("#djA").css("background-color", "#ffffff");
                $("#djB").css("background-color", "#ffffff");
                $("#djD").css("background-color", "#ffffff");
                break;
            case 'come_off_duty':
                //获取查询表单数据
                d.workStatus='come_off_duty';
                table.reload('order-table-toolbar', {
                    where: d,
                    page: {
                        curr: 1
                    }
                });
                $("#djD").css("background-color", "#b1b0b0");
                $("#djA").css("background-color", "#ffffff");
                $("#djB").css("background-color", "#ffffff");
                $("#djC").css("background-color", "#ffffff");
                break;
            case 'LAYTABLE_TIPS':
                layer.alert('这是工具栏右侧自定义的一个图标按钮');
                break;
        };
    });


    form.on('select(selectType-filter)', function (obj) {
        $("input[name='selectValue']").val(null);

    })




    function initData() {
        $.simpleAjax('/url', 'GET', null, "application/json;charset-UTF-8",returnFunction_url);
        loadOwner($("select[name='owner']"));
    }
    function returnFunction_url(data) {
        url = data
    }
    // 初始化控件数据
    $(document).ready(function(){
        initData();
    });

});

