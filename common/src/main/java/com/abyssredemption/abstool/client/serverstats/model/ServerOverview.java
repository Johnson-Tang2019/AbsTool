package com.abyssredemption.abstool.client.serverstats.model;
public record ServerOverview(String dateKey,String weekKey,int onlinePlayers,int knownPlayers,int dau,int wau,long todayPlayTimeTicks,long weekPlayTimeTicks,long todayPlacements,long weekPlacements,long todayDeaths,long weekDeaths,boolean todayPartial,boolean weekPartial,long generatedAtMillis) {}
