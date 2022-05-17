import {Component, Input, OnInit} from '@angular/core';
import {ControlContainer, NgForm} from "@angular/forms";

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss'],
  viewProviders:[
    {provide:ControlContainer,useExisting:NgForm


    }

  ]
})
export class SearchComponent implements OnInit {
  @Input()currentTermValue: any;
  @Input()rightIcon;

  constructor() { this.rightIcon='true';}

  ngOnInit(): void {

  }


}
