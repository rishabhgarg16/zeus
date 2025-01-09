// components/AnimatedStats.js
import React, { useState, useEffect, useRef } from 'react';

const AnimatedCounter = ({ end, duration = 2000, prefix = '', suffix = '' }) => {
  const [count, setCount] = useState(0);
  const countRef = useRef(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          startCounting();
        }
      },
      { threshold: 0.5 }
    );

    if (countRef.current) {
      observer.observe(countRef.current);
    }

    return () => {
      if (countRef.current) {
        observer.unobserve(countRef.current);
      }
    };
  }, []);

  const startCounting = () => {
    let start = 0;
    const step = end / (duration / 16);
    const timer = setInterval(() => {
      start += step;
      if (start > end) {
        setCount(end);
        clearInterval(timer);
      } else {
        setCount(Math.floor(start));
      }
    }, 16);
  };

  return (
    <div ref={countRef} className="text-4xl font-bold text-blue-600 mb-2">
      {prefix}
      {count.toLocaleString()}
      {suffix}
    </div>
  );
};

const AnimatedStats = () => {
  const stats = [
    { number: 1000000, label: "Downloads", suffix: "+" },
    { number: 750000, label: "Active Users", suffix: "+" },
    { number: 5000000, label: "Predictions Made", suffix: "+" }
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
      {stats.map((stat, index) => (
        <div
          key={index}
          className="bg-white p-6 rounded-lg shadow-lg transform hover:scale-105 transition-transform"
        >
          <AnimatedCounter
            end={stat.number}
            duration={2000}
            suffix={stat.suffix}
          />
          <div className="text-gray-600">{stat.label}</div>
        </div>
      ))}
    </div>
  );
};

export default AnimatedStats;