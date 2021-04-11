package cn.coegle18.smallsteps.util

import android.os.Handler
import android.util.Log
import cn.coegle18.smallsteps.States
import cn.coegle18.smallsteps.entity.NJUSTBill
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val BILL = "BILL"

class NJUSTScraper(private val userId: String,
                   private val passWord: String,
                   handler: Handler,
                   private val oldLatestTime: OffsetDateTime?) {
    companion object {
        private const val loginUrl = "http://ids.njust.edu.cn/authserver/login?service=http%3A%2F%2Fehall.njust.edu.cn%2Flogin%3F"
        private const val getJSessionIdUrl = "http://ehall.njust.edu.cn/jsonp/userDesktopInfo.json?type=&_=1603869736672"
        private const val queryUrl = "http://ehall.njust.edu.cn/appShow?appId=06001601040002"
        private const val loginInSucceedUrl = "http://ehall.njust.edu.cn/new/index.html"

        private const val dropDownList = "3"
        private const val eventTarget = "dlconsume"
        private const val eventArgument = "Page$"
        const val errorPassMsg = "用户名或者密码有误"
    }

    private val messageUtil = MessageUtil(handler)
    private val client: OkHttpClient

    init {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.cookieJar(MyCookieJar())
        client = clientBuilder.build()
    }

    private fun buildParamsToPaging(body: String, page: Int): FormBody {
        val doc = Jsoup.parse(body)
        val viewState = doc.select("#__VIEWSTATE").`val`()
        val eventValidation = doc.select("#__EVENTVALIDATION").`val`()
        val formBuilder = FormBody.Builder()
        formBuilder.apply {
            add("__VIEWSTATE", viewState)
            add("__EVENTVALIDATION", eventValidation)
            add("DropDownList1", dropDownList)
        }
        if (page != -1) { // 翻页
            formBuilder.apply {
                add("__EVENTTARGET", eventTarget)
                add("__EVENTARGUMENT", eventArgument + page)
            }
        }
        return formBuilder.build()
    }

    //
    fun parseBills(body: String, isFinished: Boolean = false, isFirstPage: Boolean = false): Boolean {
        val responseDocument = Jsoup.parse(body)

        val rowSize = responseDocument.select("#dlconsume > tbody > tr").size - 2
        repeat(rowSize) {
            val rowContext = responseDocument.select("#dlconsume > tbody > tr")[it + 1]
            val date = rowContext.select("td")[0].text()
            val money = rowContext.select("td")[1].text()
            val tradeType = rowContext.select("td")[2].text()
            val remark = rowContext.select("td")[3].text()
            Log.d("scraper", "$date, $money, $tradeType, $remark")
            val njustBill = NJUSTBill(date, money.toDouble(), tradeType, remark)

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedDate = LocalDateTime.parse(njustBill.dateStr, formatter).atZone(TimeZone.getDefault().toZoneId()).toOffsetDateTime()
            if (oldLatestTime != null && formattedDate <= oldLatestTime) { // 当没有新的账单时，直接结束
                Log.d("return", "没有新的账单")
                messageUtil.sendMessage(States.FINISHED)
                return false
            }
            if (isFirstPage && it == 0) {
                Log.d("return", "新的时间：$formattedDate")
                messageUtil.sendMessage(States.SET_NEW_LATEST_TIME, null, formattedDate)
            }
            messageUtil.sendMessage(States.GET_BILLS_SUCCESS, njustBill)
        }
        if (isFinished) {
            messageUtil.sendMessage(States.FINISHED)
        }
        return true
    }

    // 查询每页的账单
    private fun queryEachPage(page: Int, totalPage: Int, formBody: FormBody, url: String) {
        messageUtil.sendMessage(States.GET_BILLS_PROGRESSING)
        val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                messageUtil.sendMessage(States.GET_BILLS_FAILED)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val needMore = parseBills(responseBody, page == totalPage)
                    if (page < totalPage && needMore) {
                        val requestFormBody = buildParamsToPaging(responseBody, page + 1)
                        queryEachPage(page + 1, totalPage, requestFormBody, url)
                    }
                }
            }
        })


    }


    // 查询过去 30 天的账单，得到总页数
    private fun queryLast30DaysBill(formBody: FormBody, url: String) {
        messageUtil.sendMessage(States.GET_TOTAL_PAGES_PROGRESSING)
        val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                messageUtil.sendMessage(States.GET_TOTAL_PAGES_FAILED)
            }

            override fun onResponse(call: Call, response: Response) {
                messageUtil.sendMessage(States.GET_TOTAL_PAGES_SUCCESS)
                Log.d("scraper", "**Query 30 days bills response: $response")
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    // Log.d("scraper", "**Query 30 days bills response body: ${response.body?.string()}")
                    val pageNum = Jsoup.parse(responseBody).select("#dlconsume > tbody > tr:nth-child(17) > td > font > table > tbody > tr > td")?.last()?.text()?.toInt()
                    val needMore = parseBills(responseBody, pageNum == null, true)
                    if (needMore) {
                        pageNum?.let {
                            val nextRequestFormBody = buildParamsToPaging(responseBody, 2)
                            queryEachPage(2, pageNum, nextRequestFormBody, url)
                        }
                    }
                }
            }
        })
    }

    // 去查询页面
    private fun goToQueryPage() {
        messageUtil.sendMessage(States.OPEN_QUERY_PAGE_PROGRESSING)
        val request = Request.Builder()
                .url(queryUrl)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                messageUtil.sendMessage(States.OPEN_QUERY_PAGE_FAILED)
            }

            override fun onResponse(call: Call, response: Response) { // 到达查询页面
                messageUtil.sendMessage(States.OPEN_QUERY_PAGE_SUCCESS)
                Log.d("scraper", "Query page response: $response")
                val body = response.body?.string()
                if (body != null) {
                    val formBody = buildParamsToPaging(body, -1)
                    val url = response.request.url
                    queryLast30DaysBill(formBody, url.toString())
                }
            }
        })
    }

    // 获取新的 JSessionId
    private fun getNewJSessionId() {
        messageUtil.sendMessage(States.SESSION_ID_PROGRESSING)
        val request = Request.Builder()
                .url(getJSessionIdUrl)
                .build()
        Log.d("scraper", "**Get newJSessionId request: $request")
        Log.d("scraper", "**Get newJSessionId header: ${request.headers}")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                messageUtil.sendMessage(States.SESSION_IS_FAILED)
            }

            override fun onResponse(call: Call, response: Response) {
                messageUtil.sendMessage(States.SESSION_ID_SUCCESS)
                Log.d("scraper", "**Get newJSessionId response: $response")
                Log.d("scraper", "**Get newJSessionId header: ${response.headers}")
                goToQueryPage()
            }
        })
    }

    // 登录
    private fun login(formParams: Map<String, String>) {
        messageUtil.sendMessage(States.LOGIN_PROGRESSING)
        val formBuilder = FormBody.Builder()
        for (param in formParams) {
            formBuilder.add(param.key, param.value)
        }
        val body = formBuilder.build()

        val request = Request.Builder()
                .url(loginUrl)
                .post(body)
                .build()
        Log.d("scraper", "**Login request: $request")
        Log.d("scraper", "**Login header: ${request.headers}")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                messageUtil.sendMessage(States.LOGIN_FAILED)
                Log.d("error message", "登录失败")
            }

            override fun onResponse(call: Call, response: Response) {

                Log.d("scraper", "**Login response: $response")
                val responseBody = response.body?.string()
                if (response.request.url.toString().contains(loginInSucceedUrl)) {
                    messageUtil.sendMessage(States.LOGIN_SUCCESS)
                    getNewJSessionId()
                } else {
                    if (responseBody?.contains(errorPassMsg) == true) {
                        messageUtil.sendMessage(States.LOGIN_FAILED_ERROR_PASS)
                    }
                    messageUtil.sendMessage(States.LOGIN_FAILED)
                }
            }
        })
    }

    // 获取登录的参数
    @ExperimentalStdlibApi
    fun getLoginParam() {
        messageUtil.sendMessage(States.OPEN_WEB_PROGRESSING)
        val request = Request.Builder()
                .url(loginUrl)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                messageUtil.sendMessage(States.OPEN_WEB_FAILED)
                Log.d("error message", "没有打开登录网站")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    messageUtil.sendMessage(States.OPEN_WEB_SUCCESS)
                    val s = response.body?.string()
                    val res = Jsoup.parse(s)
                    val lt = res.select("#casLoginForm > input[type=hidden]:nth-child(6)").`val`()
                    val dllt = res.select("#casLoginForm > input[type=hidden]:nth-child(7)").`val`()
                    val execution = res.select("#casLoginForm > input[type=hidden]:nth-child(8)").`val`()
                    val eventId = res.select("#casLoginForm > input[type=hidden]:nth-child(9)").`val`()
                    val rmShown = res.select("#casLoginForm > input[type=hidden]:nth-child(10)").`val`()
                    val pwdDefaultEncryptSalt = res.select("#pwdDefaultEncryptSalt").`val`()

                    val formParams: MutableMap<String, String> = mutableMapOf()
                    formParams["username"] = userId
                    formParams["password"] = AESUtil.getEncryptedPassword(passWord, pwdDefaultEncryptSalt)
                    formParams["lt"] = lt
                    formParams["dllt"] = dllt
                    formParams["execution"] = execution
                    formParams["_eventId"] = eventId
                    formParams["rmShown"] = rmShown
                    Log.d("scraper", formParams.toString())

                    // 真正登录
                    login(formParams)
                } else {
                    messageUtil.sendMessage(States.OPEN_WEB_FAILED)
                }

            }
        })
    }
}

class MyCookieJar : CookieJar {
    private val cookiesMap: MutableMap<String, Cookie> = mutableMapOf()
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        for (cookie in cookies) {
            cookiesMap[cookie.name] = cookie
        }
        Log.d("scraper", "-saved cookies: $cookiesMap")
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        Log.d("scraper", "-using cookies: $cookiesMap")
        return cookiesMap.toList().map { it.second }
    }
}
