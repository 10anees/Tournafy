package com.example.tournafy.domain.enums;

//EntityStatus represents lifecycle state of domain entities (Match, Series, Tournament).
public enum EntityStatus {
	DRAFT,          // Created locally, not yet started
	SCHEDULED,      // Scheduled to start in future
	IN_PROGRESS,    // Live/running
	PAUSED,         // Temporarily halted
	COMPLETED,      // Finished with a result
	ABANDONED,      // Stopped without a result (e.g., weather)
	CANCELLED       // Cancelled before start
}
