// pages/how-it-works.js
import React from 'react';
import Layout from '../components/Layout';
import { Download, UserPlus, Wallet, Target } from 'lucide-react';

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
      title: "Add Funds",
      description: "Add money to your wallet using UPI, Net Banking, or cards.",
      detail: "100% secure payments with instant credit."
    },
    {
      icon: Target,
      title: "Start Predicting",
      description: "Choose matches and make your predictions.",
      detail: "Multiple prediction options available for each match."
    }
  ];

  const predictionTypes = [
    {
      title: "Match Winner",
      description: "Predict which team will win the match",
      example: "Example: Will India win vs Australia?"
    },
    {
      title: "Player Performance",
      description: "Predict individual player achievements",
      example: "Example: Will Virat Kohli score a century?"
    },
    {
      title: "Match Events",
      description: "Predict specific events during the match",
      example: "Example: Total runs in first 6 overs?"
    }
  ];

  return (
    <Layout>
      {/* Hero Section */}
      <div className="bg-blue-600 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-4xl font-bold mb-6">How HIT11 Works</h1>
          <p className="text-xl max-w-2xl mx-auto">
            Making cricket predictions has never been easier. Follow these simple steps to get started.
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
                    <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                      <Icon className="w-8 h-8 text-blue-600" />
                    </div>
                    {index < steps.length - 1 && (
                      <div className="hidden md:block absolute top-8 left-[60%] w-full h-0.5 bg-blue-100"></div>
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

      {/* Prediction Types */}
      <div className="bg-gray-50 py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Types of Predictions</h2>
          <div className="grid md:grid-cols-3 gap-8">
            {predictionTypes.map((type, index) => (
              <div key={index} className="bg-white p-6 rounded-lg shadow-lg">
                <h3 className="text-xl font-semibold mb-4">{type.title}</h3>
                <p className="text-gray-600 mb-4">{type.description}</p>
                <p className="text-sm text-blue-600">{type.example}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* How to Win Section */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">How to Win</h2>
          <div className="bg-white rounded-lg shadow-lg p-8">
            <div className="grid md:grid-cols-2 gap-8">
              <div>
                <h3 className="text-xl font-semibold mb-4">Winning Strategy</h3>
                <ul className="space-y-4">
                  <li className="flex items-start">
                    <span className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 flex-shrink-0 mt-1">1</span>
                    <p>Research teams and players performance history</p>
                  </li>
                  <li className="flex items-start">
                    <span className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 flex-shrink-0 mt-1">2</span>
                    <p>Analyze current form and playing conditions</p>
                  </li>
                  <li className="flex items-start">
                    <span className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 flex-shrink-0 mt-1">3</span>
                    <p>Make informed predictions based on data</p>
                  </li>
                  <li className="flex items-start">
                    <span className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 flex-shrink-0 mt-1">4</span>
                    <p>Track your prediction history and learn</p>
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
      <div className="bg-blue-600 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-6">Ready to Start Your Journey?</h2>
          <p className="text-xl mb-8">Download HIT11 now and start making predictions!</p>
          <button className="bg-white text-blue-600 px-8 py-4 rounded-lg font-semibold hover:bg-blue-50">
            Download Now
          </button>
        </div>
      </div>
    </Layout>
  );
}