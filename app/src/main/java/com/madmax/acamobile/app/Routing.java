package com.madmax.acamobile.app;

public class Routing {
    //public static String DOMAIN="http://www.madmaxmm.online/";
    public static String DOMAIN="http://192.168.110.17/";
    public static final String API=DOMAIN+"acamobile/api/";
    public static final String PROFILE_URL=DOMAIN+"acamobile/uploads/profiles/";
    public static final String GROUP_COVER_URL=DOMAIN+"acamobile/uploads/groups/";

    //Register and login
    public static final String SIGN_UP=API+"signup.php";
    public static final String CHECK_EMAIL=API+"checkemail.php";
    public static final String CHECK_AUTH=API+"auth.php";
    public static final String LOGIN=API+"login.php";

    //edit profile
    public static final String GET_PROFILE=API+"getprofile.php";
    public static final String UPLOAD_PROFILE_IMAGE=API+"updateprofileimage.php";
    public static final String UPDATE_PROFILE_DATA=API+"updateprofiledata.php";

    //products
    public static final String GET_PRODUCT_LIST=API+"products/getproducts.php";

    //Stocks
    public static final String ADD_NEW_STOCK=API+"stocks/add.php";
    public static final String GET_STOCKS=API+"stocks/get.php";
    public static final String PRODUCT_LEFT_BY_STOCK=API+"stocks/productleft.php";
    public static final String PRODUCT_LEFT_BY_ONE_STOCK=API+"stocks/productleftbyonestock.php";
    public static final String UPDATE_ITEM_LEFT=API+"stocks/updateitemleft.php";
    public static final String TRANSFER_PRODUCT=API+"stocks/transferproduct.php";

    //groups
    public static final String CREATE_GROUP=API+"groups/create.php";
    public static final String GET_MY_GROUPS=API+"groups/mygroups.php";
    public static final String GET_GROUP_MEMBERS=API+"groups/getmembers.php";
    public static final String ADD_GROUP_MEMBERS=API+"groups/addmembers.php";
    public static final String UPDATE_GROUP=API+"groups/update.php";
    public static final String UPDATE_GROUP_IMAGE=API+"groups/updateimage.php";
    public static final String GET_GROUP_DETAIL=API+"groups/getdetail.php";
    public static final String DISABLE_GROUP=API+"groups/disable.php";
    public static final String GET_MEMBER_PROFILE=API+"groups/memberprofile.php";
    public static final String COMPARE_TARGET_PLAN_AND_ORDER_RATE=API+"groups/gettargetplanandorderrate.php";


    //partner groups
    public static final String GET_PARTNER_GROUP=API+"groups/partnergroups.php";
    public static final String GET_ORDER_GROUPS=API+"groups/getordergroups.php";
    public static final String GET_ABOUT_GROUP=API+"groups/about.php";

    //users
    public static final String SEARCH_USER_BY_PHONE=API+"users/searchbyphone.php";

    //businesses
    public static final String SEND_ORDER=API+"businesses/sendorder.php";
    public static final String GET_MY_ORDERS=API+"businesses/getmyorders.php";
    public static final String GET_ORDERS=API+"businesses/getorders.php";
    public static final String GET_ORDER_DETAILS=API+"businesses/getorderdetail.php";
    public static final String UPDATE_EXTRA_COST=API+"businesses/updateextracost.php";
    public static final String SOLD_OUT_ORDER=API+"businesses/soldoutorder.php";
    public static final String RECEIVED_ORDER=API+"businesses/reveivedorder.php";
    public static final String CANCEL_ORDER=API+"businesses/cancelorder.php";
    public static final String ADD_SALE=API+"businesses/addsale.php";
    public static final String GET_SALE=API+"businesses/getsales.php";
    public static final String GET_SALE_DETAIL=API+"businesses/getsaledetail.php";
    public static final String GET_ORDER_BY_AGENT=API+"businesses/getorderbyagent.php";
    public static final String CANCEL_ORDER_BY_ADMIN=API+"businesses/cancelorder_admin.php";

    //target plan
    public static final String ADD_TARGET_PLAN=API+"targetplan/add.php";
    public static final String GET_TARGET_PLAN=API+"targetplan/get.php";
    public static final String UPDATE_TARGET_PLAN_ITEM_COUNT=API+"targetplan/updateitemquantity.php";
    public static final String GET_TARGET_PLAN_DETAILS=API+"targetplan/getdetails.php";
    public static final String UPDATE_GROUP_TARGET_PLAN=API+"targetplan/updategrouptargetplan.php";
    public static final String DELETE_TARGET_PLAN=API+"targetplan/delete.php";


    //chart
    public static final String CHART_SALE_AND_ORDER=API+"chart/orderandsale.php";
    public static final String CHART_PROFIT_PER_MONTH=API+"chart/profitpermonth.php";
    public static final String CHART_RETAIL_AND_ORDER=API+"chart/retailandagent.php";
    public static final String CHART_SALE_RATE_IN_A_PRODUCT=API+"chart/salerateforaitem.php";
    public static final String CHART_GROUP_SALE_AND_ORDER=API+"chart/group/orderandsale.php";
    public static final String CHART_GROUP_SALE_RATE_IN_A_PRODUCT=API+"chart/group/salerateforaitem.php";
    public static final String CHART_GROUP_FILTER_ORDER_RATE=API+"chart/group/filter_order_rate.php";

    //user and password
    public static final String CHECK_CURRENT_PASSWORD=API+"users/checkpassword.php";
    public static final String RESET_PASSWORD_BY_USER=API+"users/resetpassword.php";
    public static final String GENERATE_OTP=API+"users/generate_otp.php";
    public static final String CHECK_OTP=API+"users/check_otp.php";
    public static final String RESET_PASSWORD_WITH_OTP=API+"users/reset_password_with_opt.php";
    public static final String CHECK_ACC_VALIDATION=API+"users/check_validation.php";

    //app update
    public static final String CHECK_UPDATE=API+"versioncontrol.php";
}
