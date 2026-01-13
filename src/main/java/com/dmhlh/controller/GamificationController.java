package com.dmhlh.controller;

import com.dmhlh.entity.*;
import com.dmhlh.security.CustomUserDetails;
import com.dmhlh.service.GamificationService;
import com.dmhlh.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student/leaderboard")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;
    private final UserService userService;

    @GetMapping
    public String leaderboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User currentUser = userService.findById(userDetails.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get user's points and rank
        UserPoints userPoints = gamificationService.getOrCreateUserPoints(currentUser);
        int userRank = gamificationService.getUserRank(currentUser);
        
        // Get leaderboard
        List<UserPoints> leaderboard = gamificationService.getLeaderboard();
        
        // Get badges
        List<UserBadge> userBadges = gamificationService.getUserBadges(currentUser);
        List<Badge> allBadges = gamificationService.getAllBadges();
        List<Long> earnedBadgeIds = userBadges.stream()
                .map(ub -> ub.getBadge().getId())
                .collect(Collectors.toList());
        
        // Get recent transactions
        List<PointTransaction> recentTransactions = gamificationService.getRecentTransactions(currentUser, 10);
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userPoints", userPoints);
        model.addAttribute("userRank", userRank);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("userBadges", userBadges);
        model.addAttribute("allBadges", allBadges);
        model.addAttribute("earnedBadgeIds", earnedBadgeIds);
        model.addAttribute("recentTransactions", recentTransactions);
        
        return "student/leaderboard";
    }
}
