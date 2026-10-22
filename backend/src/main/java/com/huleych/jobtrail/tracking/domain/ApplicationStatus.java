package com.huleych.jobtrail.tracking.domain;

public enum ApplicationStatus {
    WISHLIST,
    APPLIED,
    INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN;

    public boolean canTransferTo(ApplicationStatus to){
        return switch (this){
            case WISHLIST -> to == APPLIED || to==WITHDRAWN;
            case APPLIED, INTERVIEW -> to == INTERVIEW || to == OFFER || to==REJECTED || to==WITHDRAWN;
            case OFFER,REJECTED,WITHDRAWN -> false;

        };
    }
}
