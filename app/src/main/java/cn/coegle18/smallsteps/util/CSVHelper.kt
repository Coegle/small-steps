package cn.coegle18.smallsteps.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import cn.coegle18.smallsteps.entity.BillView

object CSVHelper {
    /**
     * 保存的文件在 Download 目录下
     */
    private val head = listOf("时间", "分类", "子分类", "类型", "金额", "流入账户", "流出账户", "备注")
    fun saveTextFile(mContext: Context, content: List<BillView>): Boolean {
        val contentUri: Uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        //创建ContentValues对象，准备插入数据
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Downloads.MIME_TYPE, "text/csv") //文件格式
        contentValues.put(MediaStore.Downloads.DATE_TAKEN, System.currentTimeMillis())
        contentValues.put(
            MediaStore.Downloads.DISPLAY_NAME,
            "导出账单_" + System.currentTimeMillis()
        ) //文件名字
        val fileUri = mContext.contentResolver.insert(contentUri, contentValues)
        val outputStream = fileUri?.let { mContext.contentResolver.openOutputStream(it) }
        if (outputStream != null) {
            outputStream.write(head.joinToString(",", "", "\n").toByteArray())
            for (bill in content) {
                outputStream.write(bill.toSimpleString().toByteArray())
            }
            outputStream.flush()
            return true
        }
        return false
    }
}