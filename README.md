## 智能网格后台交接

接口文档访问路径：http://{主机名}:9000/grid-server/api/doc.html

### 中短期预报质量评分接口
- 预警消息评分接口：com.galaxy.score.controller.MediumShortController.warningMessage
- 预警消息评定详情接口：com.galaxy.score.controller.MediumShortController.warningDetail
- 降水过程评分接口：com.galaxy.score.controller.MediumShortController.rainProgress
- 降水过程评定详情接口：com.galaxy.score.controller.MediumShortController.rainDetail
- 暴雨公众预报接口：com.galaxy.score.controller.MediumShortController.rainstormPublic
- 暴雨公众预报详情接口：com.galaxy.score.controller.MediumShortController.rainstormDetail
- 每日预报评分接口：com.galaxy.score.controller.MediumShortController.dailyForecast
                    com.galaxy.score.controller.MediumShortController.getScoreByYear
- 首席天气公报接口：com.galaxy.score.controller.MediumShortController.weatherPublic
- 网格预报评分接口：com.galaxy.score.controller.MediumShortController.townForecastScore
                    com.galaxy.score.controller.MediumShortController.townForecasterScore
- 每日预报实况对比接口：com.galaxy.score.controller.MediumShortController.getLiveObtData
                        com.galaxy.score.controller.MediumShortController.getLiveObtData_cal
                        com.galaxy.score.controller.MediumShortController.getWFGribData
                        com.galaxy.score.controller.MediumShortController.getChartLiveHourObt
                        com.galaxy.score.controller.MediumShortController.getChartLiveHourObtCal
                        com.galaxy.score.controller.MediumShortController.getChartLiveHourGrib
                        com.galaxy.score.controller.MediumShortController.getChartWfGrib
                        com.galaxy.score.controller.MediumShortController.getLiveGribData
- 降水检验接口：com.galaxy.score.controller.MediumShortController.rainScore
- 降水检验2接口：com.galaxy.score.controller.MediumShortController.rainScore2
- 温度检验接口：com.galaxy.score.controller.MediumShortController.tempScore

### 短临预报质量评分接口
- 降水检验接口：com.galaxy.score.controller.ShortApproachController.rainScore
- 温度检验接口：com.galaxy.score.controller.ShortApproachController.temScore
- 强降水检验接口：com.galaxy.score.controller.ShortApproachController.heavyRainScore
- 雷暴检验接口：com.galaxy.score.controller.ShortApproachController.thunderScore
- 雷暴大风检验接口：com.galaxy.score.controller.ShortApproachController.thunderstormScore
- 冰雹检验接口：com.galaxy.score.controller.ShortApproachController.hailScore
- 省级预警预报质量接口：com.galaxy.score.controller.ShortApproachController.provincialWarning
- 省级预警评定详情接口：com.galaxy.score.controller.ShortApproachController.provincialDetail
- 市级预警预报质量接口：com.galaxy.score.controller.ShortApproachController.cityWarning
- 市级预警评定详情接口：com.galaxy.score.controller.ShortApproachController.cityDetail
- 预警有效性评定接口：com.galaxy.score.controller.ShortApproachController.cityWarningEff
                      com.galaxy.score.controller.ShortApproachController.districtWarningEff
- 预警有效性评定详情接口：com.galaxy.score.controller.ShortApproachController.cityDetailEff
- 强降水监测警报接口：com.galaxy.score.controller.ShortApproachController.heavyRainMonitor
- 强降水监测警报详情接口：com.galaxy.score.controller.ShortApproachController.heavyDetail

### 客观预报竞赛
- 降水评分接口：com.galaxy.score.controller.ObjectiveController.checkRainScore
- 温度评分接口：com.galaxy.score.controller.ObjectiveController.checkTemScore
- 短时强降水、雷暴大风评分接口：com.galaxy.score.controller.ObjectiveController.checkHeavyScore
- 预报及实况监测接口：com.galaxy.score.controller.ObjectiveController.forecastMonitor
                      com.galaxy.score.controller.ObjectiveController.factMonitor

### 省级预报竞赛
- 首席岗预报员评分：com.galaxy.score.controller.ProvincialController.getChiefScore
- 中短期领班预报员评分：com.galaxy.score.controller.ProvincialController.getForemanScore
- 中短期夜班预报员评分：com.galaxy.score.controller.ProvincialController.getNightShiftScore
- 短临岗预报员评分：com.galaxy.score.controller.ProvincialController.getShortTermScore
- 客观预报岗评分：com.galaxy.score.controller.ProvincialController.getObjectiveScore
- 市级评分：com.galaxy.score.controller.ProvincialController.getCityScore
- 市级预报员评分：com.galaxy.score.controller.ProvincialController.getForecasterScore