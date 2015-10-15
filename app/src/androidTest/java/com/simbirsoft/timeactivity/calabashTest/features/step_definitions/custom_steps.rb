Given /^I press the "([^\"]*)" button$/ do |text|
  tap_when_element_exists("android.widget.Button {text CONTAINS[c] '#{text}'}")
end

When /^I press image button number (\d+)$/ do |index|
  tap_when_element_exists("android.widget.ImageButton index:#{index.to_i-1}")
end

When(/^I touch plus button$/) do
  touch("com.melnykov.fab.FloatingActionButton id:'floatingButton'")
end

When(/^I create task with name "(.*?)"$/) do |name|
  query("TintEditText id:'edit'", {:setText => name})
end

When /^I touch save button$/ do
    touch("ImageButton")
end

When /^I start edit task with name "(.*?)"$/ do |name|
    touch("TextView text:'#{name}' parent CardView descendant ImageView")
end

When /^I touch delete button$/ do
    touch("ActionMenuItemView")
end

When /^I touch text "(.*?)"$/ do |text|
    touch("TextView text:'#{text}'")
end

Then /^I wait to not see text "(.*?)"$/ do |text|
    wait_for_elements_do_not_exist(["TextView text:'#{text}'"])
end

When(/^I enter tag name "(.*?)"$/) do |name|
  query("TagAutoCompleteTextView" , {:setText => name})
end

When(/^I tap task name field$/) do
  touch("TintEditText")
end

When(/^I tap task tag field$/) do
  touch("TagAutoCompleteTextView")
end