// pages/disclaimer.js
import React from 'react';
import Layout from '../components/Layout';
import { AlertTriangle, Shield, Users } from 'lucide-react';

export default function Disclaimer() {
  return (
    <Layout>
      {/* Hero Section */}
      <div className="bg-indigo-600 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-3xl font-bold">Disclaimer</h1>
          <p className="mt-2">Last Updated: February 25, 2025</p>
        </div>
      </div>

      {/* Content Section */}
      <div className="py-12">
        <div className="max-w-4xl mx-auto px-4">
          <div className="bg-yellow-50 border-l-4 border-yellow-400 p-6 mb-8">
            <div className="flex items-start">
              <AlertTriangle className="w-6 h-6 text-yellow-500 mr-4 flex-shrink-0 mt-1" />
              <div>
                <h3 className="font-bold text-lg mb-2">Important Notice</h3>
                <p>HIT11 is a skill-based gaming platform. Our games require strategy, knowledge, and skill. </p>
              </div>
            </div>
          </div>

          <div className="prose prose-lg">
            <h2>GENERAL DISCLAIMER</h2>
            <p>The information contained on the HIT11 application and website is for general information purposes only. HIT11 assumes no responsibility for errors or omissions in the contents of the Service.</p>

            <p>In no event shall HIT11 be liable for any special, direct, indirect, consequential, or incidental damages or any damages whatsoever, whether in an action of contract, negligence or other tort, arising out of or in connection with the use of the Service or the contents of the Service.</p>

            <h2>SKILL-BASED GAMING</h2>
            <p>HIT11 offers games that are predominately skill-based. The outcome of these games depends on the user's knowledge, experience, attention, and adeptness. While these games may contain elements of chance, skill is the predominant factor in determining the outcome.</p>

            <p>HIT11 explicitly does not offer games of chance or gambling services. We operate in full compliance with the laws of India, which permit skill-based gaming.</p>

            <h2>REGIONAL RESTRICTIONS</h2>
            <p>HIT11 is available only in those Indian states where skill gaming is permitted by regulations. The app is NOT available for use in Assam, Andhra Pradesh, Odisha, Telangana, Nagaland, Sikkim, and Tamil Nadu due to state-specific regulations.</p>

            <p>Users are responsible for ensuring they are accessing the Service from a jurisdiction where skill-based gaming is legal. HIT11 uses geolocation services to enforce these restrictions, but users must also comply with their local laws.</p>

            <h2>AGE RESTRICTION</h2>
            <p>HIT11 is strictly for users who are 18 years of age or older. By using our Service, you represent and warrant that you are at least 18 years old.</p>

            <h2>RESPONSIBLE GAMING</h2>
            <p>HIT11 promotes responsible gaming. We encourage users to:</p>
            <ul>
              <li>Set reasonable limits for time and money spent on gaming</li>
              <li>Not play under the influence of alcohol or drugs</li>
              <li>Not consider gaming as a source of income or a way to recover financial losses</li>
              <li>Play for entertainment purposes</li>
            </ul>

            <h2>FINANCIAL RISK</h2>
            <p>Participation in skill-based games that involve financial transactions carries inherent risk. Users should only participate with funds they can afford to lose. Past performance or success in games is not a guarantee of future results.</p>

            <h2>ACCURACY OF MATERIALS</h2>
            <p>While we strive to ensure that all information on our Service is accurate and up-to-date, we make no representations or warranties about the accuracy, reliability, completeness, or timeliness of any content. Any reliance you place on such information is strictly at your own risk.</p>

            <h2>MODIFICATIONS TO SERVICE</h2>
            <p>HIT11 reserves the right to modify or discontinue, temporarily or permanently, the Service or any features or portions thereof without prior notice. We shall not be liable for any modification, suspension, or discontinuance of the Service.</p>

            <h2>EXTERNAL LINKS</h2>
            <p>The Service may contain links to external websites that are not provided or maintained by or in any way affiliated with HIT11. We do not guarantee the accuracy, relevance, timeliness, or completeness of any information on these external websites.</p>

            <h2>CONTACT INFORMATION</h2>
            <p>If you have any questions about this Disclaimer, please contact us at legal@hit11.com.</p>
          </div>

          {/* Key Points */}
          <div className="mt-12 grid md:grid-cols-3 gap-8">
            <div className="bg-white p-6 rounded-lg shadow-lg text-center">
              <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Shield className="w-6 h-6 text-indigo-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Skill-Based Only</h3>
              <p className="text-gray-600">HIT11 offers only skill-based games</p>
            </div>

            <div className="bg-white p-6 rounded-lg shadow-lg text-center">
              <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Users className="w-6 h-6 text-indigo-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Adults Only (18+)</h3>
              <p className="text-gray-600">HIT11 is strictly for users 18 years of age or older.</p>
            </div>

            <div className="bg-white p-6 rounded-lg shadow-lg text-center">
              <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <AlertTriangle className="w-6 h-6 text-indigo-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Regional Restrictions</h3>
              <p className="text-gray-600">Not available in states where skill gaming is restricted.</p>
            </div>
          </div>
        </div>
      </div>

      {/* Responsible Gaming CTA */}
      <div className="bg-gray-50 py-12">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-2xl font-bold mb-4">Play Responsibly</h2>
          <p className="mb-6">
            HIT11 encourages users to play responsibly and within their means. If you or someone you know has a gaming problem, please seek help.
          </p>
          <button className="bg-indigo-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-indigo-700">
            Learn About Responsible Gaming
          </button>
        </div>
      </div>
    </Layout>
  );
}