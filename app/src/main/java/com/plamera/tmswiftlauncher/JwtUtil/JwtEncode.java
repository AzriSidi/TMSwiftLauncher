package com.plamera.tmswiftlauncher.JwtUtil;

import com.plamera.tmswiftlauncher.Global;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtEncode {

    public String creteToken() throws UnsupportedEncodingException {
        long currentTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
        Date exp = new Date(currentTime);
        String myToken = Jwts.builder()
                .setSubject("SwiftUser")
                .claim("staffId", Global.usernameBB)
                .claim("password",Global.passwordBB)
                .claim("imei",Global.IMEIPhone)
                .claim("imsi",Global.IMSIsimCardPhone)
                .claim("icNumber",Global.staffIcNo)
                .claim("name",Global.staffName)
                .claim("environment",Global.loginServer)
                .claim("loginStatus", Global.UserType)
                .claim("firmVer", Global.frmVersion)
                .claim("exp",exp)
                .signWith(SignatureAlgorithm.HS256, "secret".getBytes("UTF-8"))
                .setHeaderParam("typ", "JWT")
                .compact();
        return myToken;
    }
}
