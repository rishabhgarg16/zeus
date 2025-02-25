// pages/faq.js
import React, { useState } from 'react';
import Layout from '../components/Layout';
import { ChevronDown, ChevronUp } from 'lucide-react';

export default function FAQ() {
  const [openSection, setOpenSection] = useState(null);
  const [openQuestion, setOpenQuestion] = useState(null);

  const faqSections = [
    {
      title: "Getting Started",
      questions: [
        {
          q: "How do I download the HIT11 app?",
          a: "You can download the HIT11 app directly from our website. Click on the Download button at the top of the page and follow the installation instructions."
        },
        {
          q: "Is HIT11 legal in India?",
          a: "Yes, HIT11 is completely legal in India. We operate as a skill-based gaming platform in accordance with Indian laws. Games of skill are protected under Article 19(1)(g) of the Constitution of India and have been recognized by the Supreme Court."
        },
        {
          q: "How do I create an account?",
          a: "Download the app, click on Sign Up, enter your mobile number, verify with OTP, and complete your profile details."
        }
      ]
    },
    {
      title: "Games & Gameplay",
      questions: [
        {
          q: "How do the skill games work?",
          a: "Browse our selection of skill-based games, choose a game that interests you, read the rules, and start playing. Success depends on your knowledge, strategy, and analytical skills."
        },
        {
          q: "What types of games are available?",
          a: "We offer a variety of skill-based games including knowledge quizzes, word games, puzzle challenges, and strategic thinking games that test different cognitive abilities."
        },
        {
          q: "How are rewards calculated?",
          a: "Rewards are calculated based on your performance in the games, your skill level, and the specific game rules. Better performance leads to better rewards."
        }
      ]
    },
    {
      title: "Account & Wallet",
      questions: [
        {
          q: "How do I add money to my wallet?",
          a: "You can add money using UPI, Net Banking, or debit/credit cards. All transactions are secure and instant."
        },
        {
          q: "How do withdrawals work?",
          a: "Withdrawals are processed directly to your linked bank account via UPI or IMPS. The process typically takes 24-48 hours."
        },
        {
          q: "Is my money safe?",
          a: "Yes, your money is completely safe. We use bank-grade security and maintain separate user accounts for all transactions."
        }
      ]
    },
    {
      title: "Legal & Compliance",
      questions: [
        {
          q: "In which states is HIT11 available?",
          a: "HIT11 is available in all Indian states where skill-based gaming is permitted by regulations. The app is not available in Assam, Andhra Pradesh, Odisha, Telangana, Nagaland, Sikkim, and Tamil Nadu due to state-specific regulations."
        },
        {
          q: "Is there an age restriction?",
          a: "Yes, HIT11 is strictly for users who are 18 years of age or older. We implement age verification measures during the registration process."
        },
        {
          q: "How does HIT11 ensure fair play?",
          a: "We have robust systems and algorithms that ensure all games are fair and based on skill. Our platform undergoes regular audits to maintain integrity and fairness."
        }
      ]
    }
  ];

  return (
    <Layout>
      {/* Hero Section */}
      <div className="bg-indigo-600 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-4xl font-bold mb-6">Frequently Asked Questions</h1>
          <p className="text-xl max-w-2xl mx-auto">
            Find answers to common questions about HIT11
          </p>
        </div>
      </div>

      {/* FAQ Sections */}
      <div className="py-16">
        <div className="max-w-3xl mx-auto px-4">
          {faqSections.map((section, sectionIndex) => (
            <div key={sectionIndex} className="mb-8">
              <button
                className="w-full flex justify-between items-center bg-gray-50 p-4 rounded-lg hover:bg-gray-100"
                onClick={() => setOpenSection(openSection === sectionIndex ? null : sectionIndex)}
              >
                <h2 className="text-xl font-semibold">{section.title}</h2>
                {openSection === sectionIndex ? (
                  <ChevronUp className="w-6 h-6" />
                ) : (
                  <ChevronDown className="w-6 h-6" />
                )}
              </button>

              {openSection === sectionIndex && (
                <div className="mt-4 space-y-4">
                  {section.questions.map((item, questionIndex) => (
                    <div key={questionIndex} className="bg-white rounded-lg shadow">
                      <button
                        className="w-full text-left p-4 flex justify-between items-center"
                        onClick={() => setOpenQuestion(openQuestion === `${sectionIndex}-${questionIndex}` ? null : `${sectionIndex}-${questionIndex}`)}
                      >
                        <span className="font-medium">{item.q}</span>
                        {openQuestion === `${sectionIndex}-${questionIndex}` ? (
                          <ChevronUp className="w-5 h-5" />
                        ) : (
                          <ChevronDown className="w-5 h-5" />
                        )}
                      </button>
                      {openQuestion === `${sectionIndex}-${questionIndex}` && (
                        <div className="px-4 pb-4 text-gray-600">
                          {item.a}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Legal Notice */}
      <div className="bg-gray-100 py-8">
        <div className="max-w-3xl mx-auto px-4">
          <div className="bg-white p-6 rounded-lg shadow text-center">
            <p className="text-gray-600 text-sm">
              HIT11 is a skill-based gaming platform available only in those Indian states where skill gaming is permitted by regulations.
              For users 18 years and above only. Please play responsibly.
            </p>
          </div>
        </div>
      </div>

      {/* Still Have Questions */}
      <div className="bg-gray-50 py-16">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-6">Still Have Questions?</h2>
          <p className="text-xl mb-8">Our support team is here to help you 24/7</p>
          <button className="bg-indigo-600 text-white px-8 py-4 rounded-lg font-semibold hover:bg-indigo-700">
            Contact Support
          </button>
        </div>
      </div>
    </Layout>
  );
}