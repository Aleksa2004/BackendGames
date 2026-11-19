package com.example.games_extractor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GameResponse {

    private Long id;

    private String title;

    private BigDecimal price;

    private LocalDate releaseDate;

    private String imageUrl;
    
    private String metadata;
    
    private String category;
    
    private String description;
    
    public GameResponse() {}

    public GameResponse(
            Long id,
            String title,
            BigDecimal price,
            LocalDate releaseDate,
            String imageUrl,
            String metadata,
            String category,
            String description
       
    ) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.releaseDate = releaseDate;
        this.imageUrl = imageUrl;
        this.metadata=metadata;
        this.category=category;
        this.description=description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
    
}