package com.example.tournafy.domain.enums;

// Tournament format types per PRD G2.
public enum TournamentType {
	KNOCKOUT,  // Only elimination bracket
	LEAGUE,    // Round-robin/table only
	MIXED      // Group stage followed by knockout
}