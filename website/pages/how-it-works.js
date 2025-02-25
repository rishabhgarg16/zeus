import React from 'react';
import Layout from '../components/Layout';
import { Download, UserPlus, Wallet, Target, Brain, Trophy, Award } from 'lucide-react';

export default function HowItWorks() {
  const steps = [
    {
      icon: Download,
      title: "Download the App",
      description: "Get started by downloading the HIT11 app on your device.",
      detail: "Available for both Android and iOS devices."
    },
    {
      icon: UserPlus,
      title: "Create Account",
      description: "Sign up using your mobile number and verify via OTP.",
      detail: "Quick and secure registration process."
    },
    {
      icon: Wallet,
      title: "Add Game Balance",
      description: "Add funds to your account using UPI, Net Banking, or cards.",
      detail: "100% secure payments with instant credit."
    },
    {
      icon: Brain,
      title: "Start Playing",
      description: "Choose games and showcase your skills.",
      detail: "Multiple game options available for every player."
    }
  ];

  const gameTypes = [
    {
      title: "Knowledge Games",
      description: "Test your knowledge on various topics",
      example: "Example: Challenge yourself with trivia across different categories"
    },
    {
      title: "Skill Challenges",
      description: "Showcase your analytical abilities",
      example: "Example: Use your skills to solve puzzles and challenges"
    },
    {
      title: "Strategy Games",
      description: "Apply your strategic thinking",
      example: "Example: Plan your moves and outthink your opponents"
    }
  ];

  return (
    <Layout>
      {/* Hero Section */}
      <div className="bg-indigo-600 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-4xl font-bold mb-6">How HIT11 Works</h1>
          <p className="text-xl max-w-2xl mx-auto">
            Playing skill-based games has never been easier. Follow these simple steps to get started.
          </p>
        </div>
      </div>

      {/* Steps Section */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <div className="grid md:grid-cols-4 gap-8">
            {steps.map((step, index) => {
              const Icon = step.icon;
              return (
                <div key={index} className="text-center">
                  <div className="relative">
                    <div className="w-16 h-16 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-4">
                      <Icon className="w-8 h-8 text-indigo-600" />
                    </div>
                    {index < steps.length - 1 && (
                      <div className="hidden md:block absolute top-8 left-[60%] w-full h-0.5 bg-indigo-100"></div>
                    )}
                  </div>
                  <h3 className="text-xl font-semibold mb-2">{step.title}</h3>
                  <p className="text-gray-600 mb-2">{step.description}</p>
                  <p className="text-sm text-gray-500">{step.detail}</p>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Game Types */}
      <div className="bg-gray-50 py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Types of Games</h2>
          <div className="grid md:grid-cols-3 gap-8">
            {gameTypes.map((type, index) => (
              <div key={index} className="bg-white p-6 rounded-lg shadow-lg">
                <h3 className="text-xl font-semibold mb-4">{type.title}</h3>
                <p className="text-gray-600 mb-4">{type.description}</p>
                <p className="text-sm text-indigo-600">{type.example}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* How to Succeed Section */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">How to Succeed</h2>
          <div className="bg-white rounded-lg shadow-lg p-8">
            <div className="grid md:grid-cols-2 gap-8">
              <div>
                <h3 className="text-xl font-semibold mb-4">Success Strategy</h3>
                <ul className="space-y-4">
                  <li className="flex items-start">
                    <span className="w-6 h-6 bg-indigo-100 rounded-full flex items-center justify-center mr-3 flex-shrink-0 mt-1">1</span>
                    <p>Practice regularly to improve your skills</p>
                  </li>
                  <li className="flex items-start">
                    <span className="w-6 h-6 bg-indigo-100 rounded-full flex items-center justify-center mr-3 flex-shrink-0 mt-1">2</span>
                    <p>Learn the mechanics of each game</p>
                  </li>
                  <li className="flex items-start">
                    <span className="w-6 h-6 bg-indigo-100 rounded-full flex items-center justify-center mr-3 flex-shrink-0 mt-1">3</span>
                    <p>Make informed decisions based on patterns</p>
                  </li>
                  <li className="flex items-start">
                    <span className="w-6 h-6 bg-indigo-100 rounded-full flex items-center justify-center mr-3 flex-shrink-0 mt-1">4</span>
                    <p>Review your performance and adapt your strategy</p>
                  </li>
                </ul>
              </div>
              <div className="text-center">
                <img src="/api/placeholder/400/300" alt="Strategy" className="rounded-lg shadow-lg mx-auto" />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Getting Started CTA */}
      <div className="bg-indigo-600 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-6">Ready to Start Your Journey?</h2>
          <p className="text-xl mb-8">Download HIT11 now and start playing fun, skill-based games!</p>
          <button className="bg-white text-indigo-600 px-8 py-4 rounded-lg font-semibold hover:bg-indigo-50">
            Download Now
          </button>
        </div>
      </div>
    </Layout>
  );
}