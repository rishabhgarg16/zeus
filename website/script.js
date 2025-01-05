const slides = document.querySelectorAll(".carousel-slide");
const dots = document.querySelectorAll(".nav-dot");
let currentIndex = 0;

function showSlide(index) {
    slides.forEach((slide, i) => {
        slide.style.transform = `translateX(${(i - index) * 100}%)`;
    });
    dots.forEach((dot, i) => {
        dot.classList.toggle("active", i === index);
    });
}

function nextSlide() {
    currentIndex = (currentIndex + 1) % dots.length;
    showSlide(currentIndex);
}

// Auto-slide every 5 seconds
//setInterval(nextSlide, 50000);

// Add event listeners for dots
dots.forEach(dot => {
    dot.addEventListener("click", () => {
        currentIndex = parseInt(dot.dataset.index);
        showSlide(currentIndex);
    });
});

// Initialize
showSlide(currentIndex);
