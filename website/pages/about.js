import React from 'react';
import Layout from '../components/Layout';
import { Shield, Target, Users, Award } from 'lucide-react';

export default function About() {
  const values = [
    {
      icon: Shield,
      title: "Trust & Security",
      description: "We maintain the highest standards of security and fair play"
    },
    {
      icon: Target,
      title: "Innovation",
      description: "Continuously improving our platform with cutting-edge features"
    },
    {
      icon: Users,
      title: "Community First",
      description: "Building a strong, engaged community of gaming enthusiasts"
    },
    {
      icon: Award,
      title: "Excellence",
      description: "Committed to providing the best skill-based gaming experience"
    }
  ];

  return (
    <Layout>
      {/* Hero Section */}
      <div className="bg-indigo-600 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-4xl font-bold mb-6">About HIT11</h1>
          <p className="text-xl max-w-2xl mx-auto">
            India's leading skill-based gaming platform, revolutionizing mobile entertainment
          </p>
        </div>
      </div>

      {/* Our Story */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <div className="grid md:grid-cols-2 gap-12 items-center">
            <div>
              <h2 className="text-3xl font-bold mb-6">Our Story</h2>
              <p className="text-gray-600 mb-4">
                Founded in 2023, HIT11 emerged from a passion for gaming and technology. We recognized
                the need for a secure, user-friendly platform where players could showcase
                their skills and engage in entertaining gameplay.
              </p>
              <p className="text-gray-600 mb-4">
                Today, we're proud to serve millions of users across India, providing them with an
                exciting and reliable gaming platform that enhances their skills and knowledge.
              </p>
              <p className="text-gray-600">
                Our mission is to revolutionize mobile gaming by providing users with an
                innovative platform that rewards skill, strategy, and quick thinking.
              </p>
            </div>
            <div className="relative">
              <img
                src="/api/placeholder/600/400"
                alt="HIT11 Team"
                className="rounded-lg shadow-xl"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Our Values */}
      <div className="bg-gray-50 py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Our Values</h2>
          <div className="grid md:grid-cols-4 gap-8">
            {values.map((value, index) => {
              const Icon = value.icon;
              return (
                <div key={index} className="text-center">
                  <div className="w-16 h-16 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <Icon className="w-8 h-8 text-indigo-600" />
                  </div>
                  <h3 className="text-xl font-semibold mb-2">{value.title}</h3>
                  <p className="text-gray-600">{value.description}</p>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Achievements */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Our Achievements</h2>
          <div className="grid md:grid-cols-3 gap-8">
            <div className="bg-white p-6 rounded-lg shadow-lg text-center">
              <div className="text-4xl font-bold text-indigo-600 mb-2">1M+</div>
              <p className="text-gray-600">App Downloads</p>
            </div>
            <div className="bg-white p-6 rounded-lg shadow-lg text-center">
              <div className="text-4xl font-bold text-indigo-600 mb-2">₹1Cr+</div>
              <p className="text-gray-600">Rewards Distributed</p>
            </div>
            <div className="bg-white p-6 rounded-lg shadow-lg text-center">
              <div className="text-4xl font-bold text-indigo-600 mb-2">4.5★</div>
              <p className="text-gray-600">User Rating</p>
            </div>
          </div>
        </div>
      </div>

      {/* Team Section */}
      <div className="bg-white py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Our Leadership</h2>
          <div className="grid md:grid-cols-3 gap-8">
            {[1, 2, 3].map((member) => (
              <div key={member} className="text-center">
                <img
                  src={`/api/placeholder/200/200`}
                  alt={`Team Member ${member}`}
                  className="w-32 h-32 rounded-full mx-auto mb-4"
                />
                <h3 className="text-xl font-semibold">John Doe</h3>
                <p className="text-gray-600">Co-Founder & CEO</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Join Us CTA */}
      <div className="bg-indigo-600 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-6">Join the HIT11 Community</h2>
          <p className="text-xl mb-8">Be part of India's fastest-growing skill-based gaming platform</p>
          <button className="bg-white text-indigo-600 px-8 py-4 rounded-lg font-semibold hover:bg-indigo-50">
            Download Now
          </button>
        </div>
      </div>
    </Layout>
  );
}