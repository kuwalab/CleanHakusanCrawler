import com.fasterxml.jackson.databind.ObjectMapper
@Grab('org.jsoup:jsoup:1.8.1')
@Grab(group='com.fasterxml.jackson.core', module='jackson-core', version='2.5.1')
@Grab(group='com.fasterxml.jackson.core', module='jackson-databind', version='2.5.1')

import org.jsoup.Jsoup

final String URL_SERVER = 'http://www.city.hakusan.ishikawa.jp'
final int WAIT = 3000
final String ENCODING = 'Windows-31J'

final dayMap = ['日': 1, '月': 2, '火': 3, '水': 4, '木': 5, '金': 6, '土': 7]

def sleep = {
    Thread.sleep(WAIT)
}

// 結果
def trashUrlMap = [:]
def chouList = []

def doc = Jsoup.connect(URL_SERVER + '/k/shiminseikatsubu/kankyo/4r/gomi-k01.html').get()

// 地域の取得
def chiikiArray = doc.select('a')

// とりあえず松任のみ処理
def chiikiUrl = chiikiArray[0].attr('href')
sleep()

// あ行〜
def kanaGyouUrlArray = Jsoup.connect(URL_SERVER + chiikiUrl).get().select('a')
sleep()

// あ行、か行・・・
for (kanaGyouUrl in kanaGyouUrlArray) {
    if (kanaGyouUrl.text() == '戻る') continue

    def kanaHtml = Jsoup.connect(URL_SERVER + kanaGyouUrl.attr('href')).get()
    sleep()

    def chouUrlArray = kanaHtml.select('a')

    // 八ツ矢町、八ツ矢新町・・・
    def trashNo = 0;
    for (chouUrl in chouUrlArray) {
        if (chouUrl.text() == '戻る') continue

        link = chouUrl.attr('href')
        chouName = chouUrl.text()

        if (!trashUrlMap.containsKey(link)) {
            trashUrlMap.put(link, trashNo)
            trashNo++
        }
        chouList.add(new Chou(chouName, (int) trashUrlMap.get(link)))
    }
}

def trashTableMap = [:]
// trashUrlMapの処理
for (trashUrl in trashUrlMap) {
    def trashTableList = []
    trashTableMap.put(trashUrl.value, trashTableList)

    def monthHtml = Jsoup.connect(URL_SERVER + trashUrl.key).get()
    def monthUrlArray = monthHtml.select('a')
    def index = 0
    for (monthUrl in monthUrlArray) {
        if (index == 12) break
        index++
        def trashTable = new TrashTable()
        trashTableList.add(trashTable)

        def trashData = Jsoup.connect(URL_SERVER + monthUrl.attr('href')).get()
        sleep()

        def data = trashData.text().replaceAll('　', '') // 全角ブランクを潰す
        data = data.replaceAll(' ', '') // 半角ブランクを潰す
        println data
        // 一般ごみ
        def splits = data.split('一般ごみ毎週')[1].split('燃やす粗大ごみ')[0].split('・')
        // 2つ固定で処理する
        trashTable.nomalDay.add(dayMap.get(splits[0]))
        trashTable.nomalDay.add(dayMap.get(splits[1]))
        // 燃やす粗大ごみ
        num = data.split('燃やす粗大ごみ')[1].split('日')[0]
        trashTable.sodaiDate.add(Integer.parseInt(num))
        // 燃えないごみ
        num = data.split('燃えないごみ')[1].split('日')[0]
        trashTable.moenaiDate.add(Integer.parseInt(num))
        // 資源ごみ1
        num = data.split('ボトル・容器包装プラ）')[1].split('日')[0]
        trashTable.shigen1Date.add(Integer.parseInt(num))
        // 資源ごみ2
        num = data.split('資源ごみ（びん・缶・容器包装プラ）')[1].split('日')[0]
        trashTable.shigen2Date.add(Integer.parseInt(num))
    }
}

def mapper = new ObjectMapper()
def json = mapper.writeValueAsString(trashTableMap)
println json

json = mapper.writeValueAsString(chouList)
println json
