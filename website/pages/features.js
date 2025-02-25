import React from 'react';
import Layout from '../components/Layout';
import { Shield, Smartphone, Zap, Trophy, Wallet, Users } from 'lucide-react';

export default function Features() {
  const features = [
    {
      icon: Shield,
      title: "Secure Platform",
      description: "Bank-grade security for all your transactions and personal data"
    },
    {
      icon: Smartphone,
      title: "User-Friendly Interface",
      description: "Easy-to-use mobile app designed for seamless gaming experience"
    },
    {
      icon: Zap,
      title: "Real-Time Updates",
      description: "Get live game updates and instant results"
    },
    {
      icon: Trophy,
      title: "Exciting Rewards",
      description: "Earn rewards based on your skills and performance"
    },
    {
      icon: Wallet,
      title: "Quick Withdrawals",
      description: "Fast and hassle-free withdrawal process"
    },
    {
      icon: Users,
      title: "Active Community",
      description: "Join thousands of gaming enthusiasts"
    }
  ];

  const screenshots = [
    {
      title: "Game Selection",
      image: "/api/placeholder/300/600",
      description: "Browse a variety of skill-based games"
    },
    {
      title: "Play Games",
      image: "/api/placeholder/300/600",
      description: "Simple interface for playing games"
    },
    {
      title: "Track Progress",
      image: "/api/placeholder/300/600",
      description: "Monitor your performance and progress"
    }
  ];

  return (
    <Layout>
      {/* Hero Section */}
      <div className="bg-indigo-600 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-4xl font-bold mb-6">
            Experience the Best Skill-Based Gaming App
          </h1>
          <p className="text-xl mb-8 max-w-2xl mx-auto">
            Discover why thousands of users choose HIT11 for skill-based entertainment
          </p>
        </div>
      </div>

      {/* Features Grid */}
      <div className="py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">
            Packed with Amazing Features
          </h2>
          <div className="grid md:grid-cols-3 gap-8">
            {features.map((feature, index) => {
              const Icon = feature.icon;
              return (
                <div key={index} className="bg-white p-6 rounded-lg shadow-lg">
                  <div className="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center mb-4">
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

      {/* App Screenshots */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">
            See the App in Action
          </h2>
          <div className="grid md:grid-cols-3 gap-8">
            {screenshots.map((screen, index) => (
              <div key={index} className="text-center">
                <div className="relative inline-block">
                  <img
                    src={screen.image}
                    alt={screen.title}
                    className="rounded-3xl shadow-xl"
                  />
                  {/* Phone frame overlay */}
                  <div className="absolute inset-0 border-8 border-gray-900 rounded-3xl"></div>
                </div>
                <h3 className="text-xl font-semibold mt-6 mb-2">{screen.title}</h3>
                <p className="text-gray-600">{screen.description}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="bg-indigo-600 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-8">Ready to Start Playing?</h2>
          <button className="bg-white text-indigo-600 px-8 py-4 rounded-lg font-semibold hover:bg-indigo-50">
            Download HIT11 Now
          </button>
        </div>
      </div>
    </Layout>
  );
}