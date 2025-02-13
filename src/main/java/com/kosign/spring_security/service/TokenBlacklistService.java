// package com.kosign.spring_security.service;

// import org.springframework.stereotype.Service;

// import java.util.Map;
// import java.util.concurrent.TimeUnit;

// @Service
// public class TokenBlacklistService {
//     private final Map<String, Boolean> blacklist;

//     public TokenBlacklistService() {
//         // Create map with automatic expiration after token expiry
//         this.blacklist = Map.of(k1, v1)
//                 .expiration(24, TimeUnit.HOURS) // Adjust based on your token expiry time
//                 .build();
//     }

//     public void blacklistToken(String token) {
//         blacklist.put(token, true);
//     }

//     public boolean isBlacklisted(String token) {
//         return blacklist.containsKey(token);
//     }
// }
