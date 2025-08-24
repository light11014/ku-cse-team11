package com.example.ku_cse_team11_mobileapp.previewProvider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.ku_cse_team11_mobileapp.model.CreateNode
import com.example.ku_cse_team11_mobileapp.model.ContentType
import com.example.ku_cse_team11_mobileapp.model.Platform

class CreateNodeListPreviewProvider : PreviewParameterProvider<List<CreateNode>> {
    override val values = sequenceOf(
        listOf(
            CreateNode(
                id = 1L,
                title = "무공도 잘생긴 놈이 잘한다",
                author = "남겨진 스노우",
                type = ContentType.WEBTOON,
                description = "환생한 주인공이 무공과 잘생김으로 성장하는 무협 판타지!",
                platform = Platform.NAVERWEBTOON,
                thumbnailUrl = "https://picsum.photos/200/300",
                contentUrl = "https://novel.munpia.com/223890",
                publishDate = "2023-01-01",
                episodeCount = 69,
                tags = "무협,환생,성장",
                genre = "판타지",
                ageRating = "15세 이용가",
                updateFrequency = "주 2회",
                views = 12300,
                rating = 4.8,
                likes = 118,
                createdAt = "2023-01-01T10:00:00",
                updatedAt = "2023-06-01T12:00:00"
            ),
            CreateNode(
                id = 2L,
                title = "시간 여행자의 아내",
                author = "오드리 니페네거",
                type = ContentType.NOVEL,
                description = "시간 여행을 하는 남자와 그를 사랑하는 여자의 애절한 이야기",
                platform = Platform.KAKAOWEBTOON,
                thumbnailUrl = "https://picsum.photos/200/301",
                contentUrl = "https://kakao.com/novel/2002",
                publishDate = "2022-05-01",
                episodeCount = 10,
                tags = "로맨스,드라마",
                genre = "로맨스",
                ageRating = "전체 이용가",
                updateFrequency = "완결",
                views = 1200,
                rating = 4.5,
                likes = 25,
                createdAt = "2022-05-01T09:00:00",
                updatedAt = "2022-12-01T09:00:00"
            ),
            CreateNode(
                id = 3L,
                title = "달빛 조각사",
                author = "남희성",
                type = ContentType.NOVEL,
                description = "가상현실 게임에서 전설이 되어가는 주인공의 모험",
                platform = Platform.NOVELPIA,
                thumbnailUrl = "https://picsum.photos/200/302",
                contentUrl = "https://novelpia.com/3003",
                publishDate = "2021-08-15",
                episodeCount = 145,
                tags = "게임,판타지,모험",
                genre = "게임 판타지",
                ageRating = "12세 이용가",
                updateFrequency = "주 1회",
                views = 952,
                rating = 4.2,
                likes = 300,
                createdAt = "2021-08-15T08:00:00",
                updatedAt = "2023-07-01T15:00:00"
            )
        )
    )
}
