package com.example.ui

data class ShopGiftItem(
    val id: String,
    val title: String,
    val brand: String,
    val category: String, // "NAVER_PAY" or "GIFTICON"
    val requiredMelona: Int,
    val priceLabel: String,
    val iconEmoji: String,
    val description: String
)

val AvailableShopGifts = listOf(
    // 🟢 네이버페이 포인트 (1원 = 1메로나 기준)
    ShopGiftItem(
        id = "npay_1000",
        title = "네이버페이 포인트 1,000원권",
        brand = "네이버페이",
        category = "NAVER_PAY",
        requiredMelona = 1000,
        priceLabel = "1,000원",
        iconEmoji = "🟢",
        description = "네이버페이 온/오프라인 결제 시 현금처럼 즉시 사용 가능한 포인트"
    ),
    ShopGiftItem(
        id = "npay_3000",
        title = "네이버페이 포인트 3,000원권",
        brand = "네이버페이",
        category = "NAVER_PAY",
        requiredMelona = 3000,
        priceLabel = "3,000원",
        iconEmoji = "🟢",
        description = "쇼핑, 배달, 결제에 바로 충전해 쓸 수 있는 네이버 포인트"
    ),
    ShopGiftItem(
        id = "npay_5000",
        title = "네이버페이 포인트 5,000원권",
        brand = "네이버페이",
        category = "NAVER_PAY",
        requiredMelona = 5000,
        priceLabel = "5,000원",
        iconEmoji = "🟢",
        description = "온라인 쇼핑몰, 웹툰, 디지털 콘텐츠에서 자유롭게 사용하는 포인트"
    ),
    ShopGiftItem(
        id = "npay_10000",
        title = "네이버페이 포인트 10,000원권",
        brand = "네이버페이",
        category = "NAVER_PAY",
        requiredMelona = 10000,
        priceLabel = "10,000원",
        iconEmoji = "🟢",
        description = "쏠쏠하게 모아서 사용하는 알짜배기 1만 포인트 선물권"
    ),

    // 🍦 인기 기프티콘 (정가 1원 = 1메로나 비례 적용)
    ShopGiftItem(
        id = "gift_melona",
        title = "빙그레 메로나 아이스크림",
        brand = "빙그레",
        category = "GIFTICON",
        requiredMelona = 1200,
        priceLabel = "1,200원",
        iconEmoji = "🍦",
        description = "부드럽고 달콤한 원조 멜론맛 아이스바! 전국 편의점 교환 가능"
    ),
    ShopGiftItem(
        id = "gift_gs25",
        title = "GS25 모바일 상품권 3,000원권",
        brand = "GS25",
        category = "GIFTICON",
        requiredMelona = 3000,
        priceLabel = "3,000원",
        iconEmoji = "🏪",
        description = "전국 GS25 편의점에서 언제든 간편하게 결제하는 모바일 쿠폰"
    ),
    ShopGiftItem(
        id = "gift_baskin",
        title = "배스킨라빈스 싱글레귤러",
        brand = "배스킨라빈스",
        category = "GIFTICON",
        requiredMelona = 3900,
        priceLabel = "3,900원",
        iconEmoji = "🍨",
        description = "좋아하는 아이스크림 플레이버를 골라 먹는 달콤한 행복"
    ),
    ShopGiftItem(
        id = "gift_starbucks",
        title = "스타벅스 카페 아메리카노 T",
        brand = "스타벅스",
        category = "GIFTICON",
        requiredMelona = 4700,
        priceLabel = "4,700원",
        iconEmoji = "☕",
        description = "열심히 할 일을 실천한 나를 위한 향긋한 모닝 커피 한 잔의 여유"
    ),
    ShopGiftItem(
        id = "gift_cu",
        title = "CU 모바일 상품권 5,000원권",
        brand = "CU",
        category = "GIFTICON",
        requiredMelona = 5000,
        priceLabel = "5,000원",
        iconEmoji = "🏪",
        description = "전국 CU 편의점에서 자유롭게 사용하는 모바일 금액권"
    ),
    ShopGiftItem(
        id = "gift_oliveyoung",
        title = "올리브영 기프트카드 10,000원권",
        brand = "올리브영",
        category = "GIFTICON",
        requiredMelona = 10000,
        priceLabel = "10,000원",
        iconEmoji = "💄",
        description = "뷰티, 헬스, 라이프스타일 상품을 구매할 수 있는 기프트카드"
    )
)
