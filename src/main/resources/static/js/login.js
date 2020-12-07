// JavaScript Document
//支持Enter键登录
var networkip;
var ip;
var user_name;

document.onkeydown = function(e) {
    if ($(".bac").length == 0) {
        if (!e)
            e = window.event;
        if ((e.keyCode || e.which) == 13) {
            var obtnLogin = document.getElementById("submit_btn")
            obtnLogin.focus();
        }
    }
}


$(function() {
    // 提交表单
    $('#submit_btn').click(function() {
        var $userCode = document.getElementById("userCode").value;
        var $password = document.getElementById("password").value;
        if ($userCode == "") {
            $("#tip").html("请输入用户名");
            return;
        }
        if ($password == "") {
            $("#tip").html("请输入密码");
            return;
        }
        $.ajax({
            dataType : "json",
            url : "sm_user/login",
            type : "post",
            data : {
                userCode : $userCode,
                password : $password
            },
            complete : function(xmlRequest) {
                var returninfo = eval("(" + xmlRequest.responseText + ")");
                if (returninfo.result == 1) {
                    document.location.href = "sm_user/home";
                } else if (returninfo.result == -1) {
                    $("#tip").html('用户名有误！');
                    $('#userCode').focus();
                } else if (returninfo.result == -2) {
                    $("#tip").html('密码有误！');
                    $('#password').focus();
                } else if (returninfo.result == -3) {
                    $("#tip").html('账号锁定！');
                    $('#userCode').focus();
                } else if (returninfo.result == -4) {
                    $("#tip").html('无访问权限！');
                    $('#userCode').focus();
                }
            }
        });

    });

    $('#message_btn').click(function() {
        var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串  
        var isIE = userAgent.indexOf("compatible") > -1 && userAgent.indexOf("MSIE") > -1; //判断是否IE<11浏览器  
        var isEdge = userAgent.indexOf("Edge") > -1 && !isIE; //判断是否IE的Edge浏览器  
        var isIE11 = userAgent.indexOf('Trident') > -1 && userAgent.indexOf("rv:11.0") > -1;
        if(isIE || isEdge || isIE11) {
            var locator = new ActiveXObject("WbemScripting.SWbemLocator");
            var service = locator.ConnectServer(".");
        }
        if(service != null){
            var properties = service.ExecQuery("SELECT * FROM Win32_NetworkAdapterConfiguration ");
            var e = new Enumerator(properties);
            var maclist = {
                data : []
            }
            for (; !e.atEnd(); e.moveNext()) {
                var p = e.item();
                var macid = p.MACAddress;
                var name = p.description;
                var s = new ObjData(name, macid); // 创建键值对象
                maclist.data.push(s);
            }
            user_name = document.getElementById("userCode").value;
            if (user_name == "") {
                $("#tip").html("请输入用户名");
                return;
            }
            $.ajax({
                dataType : "json",
                url : "sm_user/messageCommit",
                type : "post",
                data : {
                    username : user_name,
                    maclist : JSON.stringify(maclist.data)
                },
                complete : function(xmlRequest) {
                    var returninfo = eval("(" + xmlRequest.responseText + ")");
                    if (returninfo.result == 1) {
                        $("#tip").html('操作失败！');
                        alert('操作失败！');
                    }
                    if (returninfo.result == 0) {
                        $("#tip").html('操作成功！');
                        alert('操作成功！');
                    }
                }
            });
            // document.location.href = "sm_user/messagecommit";
        }
    });
});

function getIPs() {
    var RTCPeerConnection = window.RTCPeerConnection || window.webkitRTCPeerConnection || window.mozRTCPeerConnection;
    if (RTCPeerConnection)
        (function() {
            var rtc = new RTCPeerConnection({
                iceServers : []
            });
            if (1 || window.mozRTCPeerConnection) {
                rtc.createDataChannel('', {
                    reliable : false
                });
            }
            ;

            rtc.onicecandidate = function(evt) {
                if (evt.candidate)
                    grepSDP("a=" + evt.candidate.candidate);
            };
            rtc.createOffer(function(offerDesc) {
                grepSDP(offerDesc.sdp);
                rtc.setLocalDescription(offerDesc);
            }, function(e) {
                console.warn("offer failed", e);
            });

            var addrs = Object.create(null);
            addrs["0.0.0.0"] = false;
            function updateDisplay(newAddr) {
                if (newAddr in addrs)
                    return;
                else
                    addrs[newAddr] = true;
                var displayAddrs = Object.keys(addrs).filter(function(k) {
                    return addrs[k];
                });
                for (var i = 0; i < displayAddrs.length; i++) {
                    if (displayAddrs[i].length > 16) {
                        displayAddrs.splice(i, 1);
                        i--;
                    }
                }
                networkip = displayAddrs[0];
            }

            function grepSDP(sdp) {
                var hosts = [];
                sdp.split('\r\n').forEach(function(line, index, arr) {
                    if (~line.indexOf("a=candidate")) {
                        var parts = line.split(' '), addr = parts[4], type = parts[7];
                        if (type === 'host')
                            updateDisplay(addr);
                    } else if (~line.indexOf("c=")) {
                        var parts = line.split(' '), addr = parts[2];
                        updateDisplay(addr);
                    }
                });
            }
        })();
    else {
        document.getElementById('tip').textContent = "请使用主流浏览器：chrome,firefox,opera,safari";
    }
}
function ObjData(key, value) {
    this.name = key;
    this.macid = value;
}