// pages/index.js
import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import GameCarousel from '../components/GameCarousel'; // Renamed from MatchCarousel
import AnimatedStats from '../components/AnimatedStats';
import TestimonialSlider from '../components/TestimonialSlider';
import { Download, TrendingUp, Shield, Trophy, Users } from 'lucide-react';

export default function Home() {
  // Sample data for components
  const games = [
    {
      title: "Trivia Master",
      status: "Live",
      description: "Test your general knowledge",
      options: { "Play Now": "Start Game", "Practice": "Free Mode" },
      image: "/api/placeholder/600/300"
    },
    {
      title: "Word Wizards",
      status: "New Game",
      description: "Challenge your vocabulary skills",
      options: { "Easy": "Beginner", "Hard": "Expert" },
      image: "/api/placeholder/600/300"
    },
    {
      title: "Brain Teasers",
      status: "Popular",
      description: "Logic puzzles to test your thinking",
      options: { "Start": "Play Now", "Learn": "Rules" },
      image: "/api/placeholder/600/300"
    }
  ];

  const testimonials = [
    {
      name: "Rahul K.",
      location: "Mumbai",
      comment: "The best skill-based gaming app! Earned ₹20,000 in my first month. The interface is super easy to use and withdrawals are instant.",
      rating: 5,
      image: "/api/placeholder/64/64"
    },
    {
      name: "Priya S.",
      location: "Delhi",
      comment: "Very user-friendly interface and quick withdrawals. I enjoy testing my knowledge and skills daily!",
      rating: 5,
      image: "/api/placeholder/64/64"
    },
    {
      name: "Amit P.",
      location: "Bangalore",
      comment: "Great platform for skill-based games! The customer support is very responsive and helpful.",
      rating: 5,
      image: "/api/placeholder/64/64"
    }
  ];

  return (
    <Layout>
      {/* Hero Section with Floating Elements */}
      <div className="relative bg-gradient-to-r from-indigo-600 to-indigo-800 text-white overflow-hidden">
        <div className="absolute inset-0">
          <div className="absolute inset-0 bg-gradient-to-r from-indigo-600 to-indigo-800 opacity-90"></div>
          <div className="absolute inset-0 bg-[url('/api/placeholder/1920/1080')] bg-cover bg-center"></div>
        </div>
        <div className="relative max-w-7xl mx-auto px-4 py-20">
          <div className="grid md:grid-cols-2 gap-12 items-center">
            <div className="space-y-8">
              <h1 className="text-5xl font-bold animate-fade-in">
                Skill-Based Games Made Simple
              </h1>
              <p className="text-xl opacity-90">
                Join millions of players enjoying skill-based games and earning rewards
              </p>
              <div className="space-y-4">
                <div className="flex flex-wrap gap-4">
                  <button className="group bg-white text-indigo-600 px-8 py-4 rounded-lg font-semibold hover:bg-indigo-50 transition-all transform hover:scale-105">
                    <div className="flex items-center space-x-2">
                      <Download className="w-5 h-5 group-hover:animate-bounce" />
                      <span>Download App</span>
                    </div>
                  </button>
                  <button className="border-2 border-white px-8 py-4 rounded-lg font-semibold hover:bg-white hover:text-indigo-600 transition-all">
                    Learn More
                  </button>
                </div>
                <div className="text-sm bg-white/20 px-3 py-1 rounded-full inline-block">
                  For 18 years and above only
                </div>
              </div>
              <div className="text-sm bg-white/10 p-4 rounded-lg backdrop-blur-sm inline-block">
                HIT11 is available only in states where skill gaming is permitted
              </div>
            </div>
            <div className="hidden md:block relative">
              <img
                src="/api/placeholder/600/400"
                alt="HIT11 App"
                className="rounded-lg shadow-2xl transform hover:scale-105 transition-transform duration-300"
              />
              <div className="absolute -bottom-6 -left-6 bg-white text-indigo-600 p-4 rounded-lg shadow-lg">
                <div className="font-bold">4.5★ Rating</div>
                <div className="text-sm">10,000+ Reviews</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Section */}
      <div className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4">
          <AnimatedStats />
        </div>
      </div>

      {/* Games Section */}
      <div className="bg-gray-50 py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Featured Games</h2>
          <GameCarousel games={games} />
        </div>
      </div>

      {/* How It Works */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Start Playing in 3 Easy Steps</h2>
          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                title: "Download the App",
                description: "Quick installation process",
                color: "indigo",
                image: "/api/placeholder/200/200"
              },
              {
                title: "Create Account & Add Funds",
                description: "Simple KYC & secure payments",
                color: "purple",
                image: "/api/placeholder/200/200"
              },
              {
                title: "Start Playing",
                description: "Choose games & showcase your skills",
                color: "blue",
                image: "/api/placeholder/200/200"
              }
            ].map((step, index) => (
              <div
                key={index}
                className="transform hover:-translate-y-2 transition-transform duration-300"
              >
                <div className={`bg-${step.color}-100 rounded-lg p-6 mb-4 relative overflow-hidden group`}>
                  <img
                    src={step.image}
                    alt={step.title}
                    className="mx-auto relative z-10 group-hover:scale-110 transition-transform duration-300"
                  />
                  <div className={`absolute inset-0 bg-${step.color}-200 transform scale-x-0 group-hover:scale-x-100 transition-transform origin-left`}></div>
                </div>
                <h3 className="text-xl font-semibold text-center mb-2">
                  {index + 1}. {step.title}
                </h3>
                <p className="text-gray-600 text-center">{step.description}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Testimonials */}
      <div className="bg-gray-50 py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">What Our Users Say</h2>
          <TestimonialSlider testimonials={testimonials} />
        </div>
      </div>

      {/* Features */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Why Choose HIT11?</h2>
          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                icon: Shield,
                title: "100% Safe & Secure",
                description: "Bank-grade security for all transactions"
              },
              {
                icon: Trophy,
                title: "Fast Withdrawals",
                description: "Get your rewards instantly via UPI"
              },
              {
                icon: Users,
                title: "24/7 Support",
                description: "Get help anytime you need it"
              }
            ].map((feature, index) => {
              const Icon = feature.icon;
              return (
                <div
                  key={index}
                  className="bg-white p-6 rounded-lg shadow-lg transform hover:-translate-y-2 transition-all duration-300 hover:shadow-xl"
                >
                  <div className="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center mb-4 group-hover:bg-indigo-200 transition-colors">
                    <Icon className="w-6 h-6 text-indigo-600" />
                  </div>
                  <h3 className="text-xl font-semibold mb-2">{feature.title}</h3>
                  <p className="text-gray-600">{feature.description}</p>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Legal Notice */}
      <div className="bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4">
          <div className="bg-white p-6 rounded-lg shadow text-center">
            <p className="text-gray-600 text-sm">
              HIT11 is a skill-based gaming platform available only in those Indian states where skill gaming is permitted by regulations.
              For users 18 years and above only. Please play responsibly.
            </p>
          </div>
        </div>
      </div>

      {/* Download CTA */}
      <div className="bg-indigo-600 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-6">Ready to Start Playing?</h2>
          <p className="text-xl mb-8">Join millions of users on HIT11</p>
          <div className="space-y-6">
            <div className="flex justify-center space-x-4">
              {['Google Play', 'App Store'].map((store) => (
                <button
                  key={store}
                  className="bg-black text-white px-8 py-4 rounded-lg flex items-center space-x-2 hover:bg-gray-900 transition-colors transform hover:scale-105"
                >
                  <img src="/api/placeholder/24/24" alt={store} className="w-6 h-6" />
                  <span>{store}</span>
                </button>
              ))}
            </div>
            <div className="text-sm bg-white/10 px-4 py-2 rounded-full inline-block">
              For 18 years and above only
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}