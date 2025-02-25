import React, { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight, Clock, Trophy } from 'lucide-react';

const GameCarousel = ({ games }) => {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [isAnimating, setIsAnimating] = useState(false);
  const [autoplay, setAutoplay] = useState(true);

  useEffect(() => {
    if (!autoplay) return;

    const timer = setInterval(() => {
      nextSlide();
    }, 5000);

    return () => clearInterval(timer);
  }, [currentSlide, autoplay]);

  const nextSlide = () => {
    if (isAnimating) return;
    setIsAnimating(true);
    setCurrentSlide((prev) => (prev + 1) % games.length);
    setTimeout(() => setIsAnimating(false), 500);
  };

  const prevSlide = () => {
    if (isAnimating) return;
    setIsAnimating(true);
    setCurrentSlide((prev) => (prev - 1 + games.length) % games.length);
    setTimeout(() => setIsAnimating(false), 500);
  };

  return (
    <div
      className="relative"
      onMouseEnter={() => setAutoplay(false)}
      onMouseLeave={() => setAutoplay(true)}
    >
      <div className="overflow-hidden rounded-xl">
        <div
          className="flex transition-transform duration-500 ease-out"
          style={{ transform: `translateX(-${currentSlide * 100}%)` }}
        >
          {games.map((game, index) => (
            <div key={index} className="w-full flex-shrink-0 px-4">
              <div className="bg-white rounded-xl shadow-lg overflow-hidden">
                <div className="relative">
                  <img
                    src={game.image}
                    alt={game.title}
                    className="w-full h-48 object-cover"
                  />
                  <div className="absolute top-0 left-0 right-0 p-4 bg-gradient-to-b from-black/50 to-transparent">
                    <div className="flex justify-between items-center text-white">
                      <h3 className="text-xl font-bold">{game.title}</h3>
                      <div className="flex items-center bg-green-500 px-3 py-1 rounded-full text-sm">
                        <Clock className="w-4 h-4 mr-1" />
                        {game.status}
                      </div>
                    </div>
                  </div>
                </div>
                <div className="p-6">
                  <div className="text-lg font-semibold mb-4">{game.description}</div>
                  <div className="grid grid-cols-2 gap-4">
                    {Object.entries(game.options).map(([key, value]) => (
                      <button
                        key={key}
                        className="group relative overflow-hidden rounded-lg bg-gray-50 p-4 transition-all hover:bg-indigo-50"
                      >
                        <div className="relative z-10">
                          <div className="font-medium capitalize">{key}</div>
                          <div className="text-indigo-600 font-bold">{value}</div>
                        </div>
                        <div className="absolute inset-0 bg-indigo-100 transform scale-x-0 group-hover:scale-x-100 transition-transform origin-left"></div>
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Navigation Buttons */}
      <button
        onClick={prevSlide}
        className="absolute left-2 top-1/2 -translate-y-1/2 bg-white/90 p-2 rounded-full shadow-lg hover:bg-white transition-colors"
        disabled={isAnimating}
      >
        <ChevronLeft className="w-6 h-6" />
      </button>
      <button
        onClick={nextSlide}
        className="absolute right-2 top-1/2 -translate-y-1/2 bg-white/90 p-2 rounded-full shadow-lg hover:bg-white transition-colors"
        disabled={isAnimating}
      >
        <ChevronRight className="w-6 h-6" />
      </button>

      {/* Slide Indicators */}
      <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex space-x-2">
        {games.map((_, index) => (
          <button
            key={index}
            onClick={() => setCurrentSlide(index)}
            className={`w-2 h-2 rounded-full transition-all ${
              currentSlide === index ? 'bg-indigo-600 w-4' : 'bg-gray-400'
            }`}
          />
        ))}
      </div>
    </div>
  );
};

export default GameCarousel;