DrugInfoScan
============

药品监管码手机查询软件，通过录入（扫描）20位药品包装盒上的药品电子监管码可以获取该药品的基本信息和流向信息，所有信息均来自中国药品电子监管网。

第一个上传的版本是1.2 beta

服务器端主要通信结构(JSON)如下：
{
"result":"SUCCESS",   //结果状态：SUCCESS|FAIL|ERROR  分别对应查询成功、查询失败（没有找到）、服务器错误,注意区分大小写
"msg":"查询完成"，     //服务器返回的信息，一般直接用于反馈提示中
   "drugInfo":        //药品信息，如果reulst:SUCCESS的时候drugInfo必须要存在
      {"drugName":" 归脾丸",
       "preSpec":"-",
       "packageSpec":"盒60克",
       "manufacturer":"河南省宛西制药股份有限公司",
       "productionDate":"2012年04月26日",
       "batchNumber":"120404",
       "validDate":"20150425",
       "licenseNumber":"国药准字Z41022230",
       "flow":"该药品已经被“河南省宛西制药股份有限公司”签收",
       }   
}

详细可以参考：http://api.aomask.com/drugInfoQuery.json?code=81061670000992714423
