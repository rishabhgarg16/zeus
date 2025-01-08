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


const jsonData = [
    {
        title: "Company",
        items: ["About Us", "Culture"]
    },
    {
        title: "Resources",
        items: ["Help Centre", "Contact Support", "What's New"]
    },
    {
        title: "Careers",
        items: ["Open Roles"]
    },
    {
        title: "Contact Us",
        items: ["help@hit11.ai", "communication@hit11.ai"]
    }
];

function generateHTMLFromJSON(data) {
    const container = document.getElementById("info-container");
    container.className = "info-columns";

    data.forEach(section => {
        const column = document.createElement("div");
        column.className = "column";

        const heading = document.createElement("h3");
        heading.textContent = section.title;
        column.appendChild(heading);

        const list = document.createElement("ul");
        section.items.forEach(item => {
            const listItem = document.createElement("li");
            listItem.textContent = item;
            list.appendChild(listItem);
        });

        column.appendChild(list);
        container.appendChild(column);
    });

    document.body.appendChild(container);
}

document.addEventListener("DOMContentLoaded", () => {
    generateHTMLFromJSON(jsonData);
});