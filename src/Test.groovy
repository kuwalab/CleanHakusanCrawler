@Grab('org.jsoup:jsoup:1.8.1')

import org.jsoup.Jsoup

final String SERVER = 'http://www.city.hakusan.ishikawa.jp'
final int WAIT = 3000
final String ENCODING = 'Windows-31J'


def doc = Jsoup.connect(SERVER + '/k/shiminseikatsubu/kankyo/4r/gomi-k01.html').get()

// 松任地域
def matto = doc.select('a')
println(matto[0])